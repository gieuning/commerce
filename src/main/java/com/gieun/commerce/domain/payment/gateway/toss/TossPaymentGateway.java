package com.gieun.commerce.domain.payment.gateway.toss;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gieun.commerce.domain.payment.gateway.PaymentCancelCommand;
import com.gieun.commerce.domain.payment.gateway.PaymentCancelResult;
import com.gieun.commerce.domain.payment.gateway.PaymentConfirmCommand;
import com.gieun.commerce.domain.payment.gateway.PaymentConfirmResult;
import com.gieun.commerce.domain.payment.gateway.PaymentGateway;
import com.gieun.commerce.domain.payment.gateway.PaymentGatewayException;
import com.gieun.commerce.global.exception.DomainExceptionCode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class TossPaymentGateway implements PaymentGateway {

  private static final String CONFIRM_PATH = "/v1/payments/confirm";
  private static final String CANCEL_PATH = "/v1/payments/{paymentKey}/cancel";
  private static final String PAYMENT_PATH = "/v1/payments/{paymentKey}";
  private static final String FIELD_PAYMENT_KEY = "paymentKey";
  private static final String FIELD_ORDER_ID = "orderId";
  private static final String FIELD_AMOUNT = "amount";
  private static final String FIELD_STATUS = "status";
  private static final String FIELD_TOTAL_AMOUNT = "totalAmount";
  private static final String FIELD_SUPPLIED_AMOUNT = "suppliedAmount";
  private static final String FIELD_VAT = "vat";
  private static final String FIELD_RECEIPT = "receipt";
  private static final String FIELD_URL = "url";
  private static final String FIELD_APPROVED_AT = "approvedAt";
  private static final String FIELD_CANCEL_REASON = "cancelReason";
  private static final String FIELD_CANCEL_AMOUNT = "cancelAmount";
  private static final String FIELD_TRANSACTION_KEY = "transactionKey";
  private static final String FIELD_CANCELED_AT = "canceledAt";
  private static final String FIELD_CODE = "code";
  private static final String FIELD_MESSAGE = "message";
  private static final String FIELD_CANCELS = "cancels";
  // Toss: 이미 취소/환불된 결제를 다시 취소할 때 내려오는 에러코드. 우리가 원하던 종료 상태이므로 멱등 성공으로 간주한다.
  private static final Set<String> IDEMPOTENT_CANCEL_CODES = Set.of(
      "ALREADY_CANCELED_PAYMENT",
      "ALREADY_REFUND_PAYMENT"
  );

  private final RestClient restClient;
  private final ObjectMapper objectMapper;

  public TossPaymentGateway(
      RestClient.Builder restClientBuilder,
      TossPaymentProperties properties,
      ObjectMapper objectMapper
  ) {
    this.restClient = restClientBuilder
        .baseUrl(properties.getBaseUrl())
        .defaultHeaders(headers -> headers.setBasicAuth(properties.getSecretKey(), ""))
        .requestFactory(requestFactory(properties))
        .build();
    this.objectMapper = objectMapper;
  }

  private SimpleClientHttpRequestFactory requestFactory(TossPaymentProperties properties) {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(properties.getConnectTimeout());
    requestFactory.setReadTimeout(properties.getReadTimeout());
    return requestFactory;
  }

  @Override
  public PaymentConfirmResult confirm(PaymentConfirmCommand command) {
    Map<String, Object> request = new LinkedHashMap<>();
    request.put(FIELD_PAYMENT_KEY, command.getPaymentKey());
    request.put(FIELD_ORDER_ID, command.getMerchantOrderId());
    request.put(FIELD_AMOUNT, command.getAmount());

    String requestPayload = writeJson(request);
    String responsePayload = post(CONFIRM_PATH, request, DomainExceptionCode.PAYMENT_APPROVAL_FAILED);
    JsonNode response = readJson(responsePayload, DomainExceptionCode.PAYMENT_APPROVAL_FAILED);

    return PaymentConfirmResult.builder()
        .paymentKey(text(response, FIELD_PAYMENT_KEY))
        .merchantOrderId(text(response, FIELD_ORDER_ID))
        .pgStatus(text(response, FIELD_STATUS))
        .totalAmount(decimal(response, FIELD_TOTAL_AMOUNT))
        .suppliedAmount(decimal(response, FIELD_SUPPLIED_AMOUNT))
        .vat(decimal(response, FIELD_VAT))
        .receiptUrl(response.path(FIELD_RECEIPT).path(FIELD_URL).asText(null))
        .approvedAt(dateTime(response, FIELD_APPROVED_AT))
        .requestPayload(requestPayload)
        .responsePayload(responsePayload)
        .build();
  }

  @Override
  public PaymentCancelResult cancel(PaymentCancelCommand command) {
    Map<String, Object> request = new LinkedHashMap<>();
    request.put(FIELD_CANCEL_REASON, command.getCancelReason());
    if (command.getCancelAmount() != null) {
      request.put(FIELD_CANCEL_AMOUNT, command.getCancelAmount());
    }

    String requestPayload = writeJson(request);

    String responsePayload;
    try {
      responsePayload = post(
          CANCEL_PATH,
          request,
          DomainExceptionCode.PAYMENT_CANCEL_FAILED,
          command.getPaymentKey()
      );
    } catch (PaymentGatewayException exception) {
      if (isIdempotentlyCanceled(exception.getPgCode())) {
        // 멱등: 이미 취소/환불된 결제는 원하던 종료 상태이므로, 결제 단건을 재조회해 실제 취소 상세를 채운다.
        return fetchCanceledResult(command.getPaymentKey(), requestPayload);
      }
      throw exception;
    }

    JsonNode response = readJson(responsePayload, DomainExceptionCode.PAYMENT_CANCEL_FAILED);
    return toCancelResult(response, requestPayload, responsePayload);
  }

  private boolean isIdempotentlyCanceled(String pgCode) {
    return pgCode != null && IDEMPOTENT_CANCEL_CODES.contains(pgCode);
  }

  // 이미 취소/환불된 결제: GET /v1/payments/{paymentKey}로 재조회해 정확한 취소 상세(취소키/금액/시각)를 채운다.
  private PaymentCancelResult fetchCanceledResult(String paymentKey, String requestPayload) {
    String responsePayload = get(PAYMENT_PATH, DomainExceptionCode.PAYMENT_CANCEL_FAILED, paymentKey);
    JsonNode response = readJson(responsePayload, DomainExceptionCode.PAYMENT_CANCEL_FAILED);
    return toCancelResult(response, requestPayload, responsePayload);
  }

  // 결제 단건(JSON)에서 최신 취소 정보를 추출해 취소 결과로 변환한다. (cancel 응답/단건 재조회 응답 공용)
  private PaymentCancelResult toCancelResult(JsonNode response, String requestPayload, String responsePayload) {
    JsonNode cancel = latestCancel(response);

    return PaymentCancelResult.builder()
        .paymentKey(text(response, FIELD_PAYMENT_KEY))
        .pgStatus(text(response, FIELD_STATUS))
        .pgCancellationKey(text(cancel, FIELD_TRANSACTION_KEY))
        .cancelAmount(decimal(cancel, FIELD_CANCEL_AMOUNT))
        .cancelledAt(dateTime(cancel, FIELD_CANCELED_AT))
        .requestPayload(requestPayload)
        .responsePayload(responsePayload)
        .build();
  }

  private String get(String path, DomainExceptionCode exceptionCode, Object... uriVariables) {
    try {
      String responsePayload = restClient.get()
          .uri(path, uriVariables)
          .retrieve()
          .body(String.class);
      if (isBlank(responsePayload)) {
        throw emptyPayloadException(exceptionCode, responsePayload);
      }
      return responsePayload;
    } catch (RestClientResponseException exception) {
      throw gatewayException(exceptionCode, exception.getResponseBodyAsString());
    } catch (RestClientException exception) {
      throw new PaymentGatewayException(exceptionCode, null, exception.getMessage(), null);
    }
  }

  private String post(String path, Object request, DomainExceptionCode exceptionCode, Object... uriVariables) {
    try {
      String responsePayload = restClient.post()
          .uri(path, uriVariables)
          .contentType(MediaType.APPLICATION_JSON)
          .body(request)
          .retrieve()
          .body(String.class);
      if (isBlank(responsePayload)) {
        throw emptyPayloadException(exceptionCode, responsePayload);
      }
      return responsePayload;
    } catch (RestClientResponseException exception) {
      throw gatewayException(exceptionCode, exception.getResponseBodyAsString());
    } catch (RestClientException exception) {
      throw new PaymentGatewayException(exceptionCode, null, exception.getMessage(), null);
    }
  }

  private PaymentGatewayException gatewayException(DomainExceptionCode exceptionCode, String responsePayload) {
    JsonNode error = readJsonOrNull(responsePayload);
    if (error == null) {
      return new PaymentGatewayException(exceptionCode, null, responsePayload, responsePayload);
    }

    return new PaymentGatewayException(
        exceptionCode,
        text(error, FIELD_CODE),
        text(error, FIELD_MESSAGE),
        responsePayload
    );
  }

  private JsonNode latestCancel(JsonNode response) {
    JsonNode cancels = response.path(FIELD_CANCELS);
    if (!cancels.isArray() || cancels.isEmpty()) {
      return objectMapper.createObjectNode();
    }
    return cancels.get(cancels.size() - 1);
  }

  private JsonNode readJson(String payload, DomainExceptionCode exceptionCode) {
    if (isBlank(payload)) {
      throw emptyPayloadException(exceptionCode, payload);
    }

    try {
      return objectMapper.readTree(payload);
    } catch (JsonProcessingException exception) {
      throw new PaymentGatewayException(exceptionCode, null, exception.getMessage(), payload);
    }
  }

  private JsonNode readJsonOrNull(String payload) {
    if (isBlank(payload)) {
      return null;
    }

    try {
      return objectMapper.readTree(payload);
    } catch (JsonProcessingException exception) {
      return null;
    }
  }

  private PaymentGatewayException emptyPayloadException(DomainExceptionCode exceptionCode, String payload) {
    return new PaymentGatewayException(exceptionCode, null, "PG 응답 본문이 비어 있습니다.", payload);
  }

  private boolean isBlank(String payload) {
    return payload == null || payload.isBlank();
  }

  private String writeJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("PG 요청 JSON 생성에 실패했습니다.", exception);
    }
  }

  private String text(JsonNode node, String fieldName) {
    if (node == null || node.path(fieldName).isMissingNode() || node.path(fieldName).isNull()) {
      return null;
    }
    return node.path(fieldName).asText();
  }

  private BigDecimal decimal(JsonNode node, String fieldName) {
    if (node == null || node.path(fieldName).isMissingNode() || node.path(fieldName).isNull()) {
      return null;
    }
    return node.path(fieldName).decimalValue();
  }

  private OffsetDateTime dateTime(JsonNode node, String fieldName) {
    String value = text(node, fieldName);
    if (value == null || value.isBlank()) {
      return null;
    }

    try {
      return OffsetDateTime.parse(value);
    } catch (DateTimeParseException exception) {
      return null;
    }
  }
}
