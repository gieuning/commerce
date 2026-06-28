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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
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
    request.put("paymentKey", command.getPaymentKey());
    request.put("orderId", command.getMerchantOrderId());
    request.put("amount", command.getAmount());

    String requestPayload = writeJson(request);
    String responsePayload = post(CONFIRM_PATH, request, DomainExceptionCode.PAYMENT_APPROVAL_FAILED);
    JsonNode response = readJson(responsePayload, DomainExceptionCode.PAYMENT_APPROVAL_FAILED);

    return PaymentConfirmResult.builder()
        .paymentKey(text(response, "paymentKey"))
        .merchantOrderId(text(response, "orderId"))
        .pgStatus(text(response, "status"))
        .totalAmount(decimal(response, "totalAmount"))
        .suppliedAmount(decimal(response, "suppliedAmount"))
        .vat(decimal(response, "vat"))
        .receiptUrl(response.path("receipt").path("url").asText(null))
        .approvedAt(dateTime(response, "approvedAt"))
        .requestPayload(requestPayload)
        .responsePayload(responsePayload)
        .build();
  }

  @Override
  public PaymentCancelResult cancel(PaymentCancelCommand command) {
    Map<String, Object> request = new LinkedHashMap<>();
    request.put("cancelReason", command.getCancelReason());
    if (command.getCancelAmount() != null) {
      request.put("cancelAmount", command.getCancelAmount());
    }

    String requestPayload = writeJson(request);
    String responsePayload = post(
        CANCEL_PATH,
        request,
        DomainExceptionCode.PAYMENT_CANCEL_FAILED,
        command.getPaymentKey()
    );
    JsonNode response = readJson(responsePayload, DomainExceptionCode.PAYMENT_CANCEL_FAILED);
    JsonNode cancel = latestCancel(response);

    return PaymentCancelResult.builder()
        .paymentKey(text(response, "paymentKey"))
        .pgStatus(text(response, "status"))
        .pgCancellationKey(text(cancel, "transactionKey"))
        .cancelAmount(decimal(cancel, "cancelAmount"))
        .cancelledAt(dateTime(cancel, "canceledAt"))
        .requestPayload(requestPayload)
        .responsePayload(responsePayload)
        .build();
  }

  private String post(String path, Object request, DomainExceptionCode exceptionCode, Object... uriVariables) {
    try {
      return restClient.post()
          .uri(path, uriVariables)
          .contentType(MediaType.APPLICATION_JSON)
          .body(request)
          .retrieve()
          .body(String.class);
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
        text(error, "code"),
        text(error, "message"),
        responsePayload
    );
  }

  private JsonNode latestCancel(JsonNode response) {
    JsonNode cancels = response.path("cancels");
    if (!cancels.isArray() || cancels.isEmpty()) {
      return objectMapper.createObjectNode();
    }
    return cancels.get(cancels.size() - 1);
  }

  private JsonNode readJson(String payload, DomainExceptionCode exceptionCode) {
    try {
      return objectMapper.readTree(payload);
    } catch (JsonProcessingException exception) {
      throw new PaymentGatewayException(exceptionCode, null, exception.getMessage(), payload);
    }
  }

  private JsonNode readJsonOrNull(String payload) {
    try {
      return objectMapper.readTree(payload);
    } catch (JsonProcessingException exception) {
      return null;
    }
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

  private LocalDateTime dateTime(JsonNode node, String fieldName) {
    String value = text(node, fieldName);
    if (value == null || value.isBlank()) {
      return null;
    }

    try {
      return OffsetDateTime.parse(value).toLocalDateTime();
    } catch (DateTimeParseException exception) {
      return LocalDateTime.parse(value);
    }
  }
}
