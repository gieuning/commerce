package com.gieun.commerce.domain.payment.gateway.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gieun.commerce.domain.payment.gateway.PaymentCancelResult;
import com.gieun.commerce.domain.payment.gateway.PaymentGatewayException;
import com.gieun.commerce.global.exception.DomainExceptionCode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

class TossPaymentGatewayTest {

  @Test
  void configuresConnectAndReadTimeouts() {
    RestClient.Builder builder = org.mockito.Mockito.mock(RestClient.Builder.class);
    RestClient restClient = org.mockito.Mockito.mock(RestClient.class);
    TossPaymentProperties properties = new TossPaymentProperties();
    properties.setBaseUrl("https://example.com");
    properties.setSecretKey("secret");
    properties.setConnectTimeout(Duration.ofSeconds(2));
    properties.setReadTimeout(Duration.ofSeconds(7));

    when(builder.baseUrl("https://example.com")).thenReturn(builder);
    when(builder.defaultHeaders(org.mockito.ArgumentMatchers.<Consumer<HttpHeaders>>any()))
        .thenReturn(builder);
    when(builder.requestFactory(any(ClientHttpRequestFactory.class))).thenReturn(builder);
    when(builder.build()).thenReturn(restClient);

    new TossPaymentGateway(builder, properties, new ObjectMapper());

    ArgumentCaptor<ClientHttpRequestFactory> requestFactoryCaptor =
        ArgumentCaptor.forClass(ClientHttpRequestFactory.class);
    verify(builder).requestFactory(requestFactoryCaptor.capture());
    ClientHttpRequestFactory requestFactory = requestFactoryCaptor.getValue();
    assertThat(requestFactory).isInstanceOf(SimpleClientHttpRequestFactory.class);
    assertThat(ReflectionTestUtils.getField(requestFactory, "connectTimeout"))
        .isEqualTo(2_000);
    assertThat(ReflectionTestUtils.getField(requestFactory, "readTimeout"))
        .isEqualTo(7_000);
  }

  @Test
  void parsesTossTimestampsWithOffset() throws Exception {
    TossPaymentGateway gateway = gateway();
    ObjectMapper objectMapper = new ObjectMapper();

    Object approvedAt = ReflectionTestUtils.invokeMethod(
        gateway,
        "dateTime",
        objectMapper.readTree("{\"approvedAt\":\"2026-06-29T12:34:56+09:00\"}"),
        "approvedAt"
    );
    Object cancelledAt = ReflectionTestUtils.invokeMethod(
        gateway,
        "dateTime",
        objectMapper.readTree("{\"canceledAt\":\"2026-06-29T13:34:56+09:00\"}"),
        "canceledAt"
    );

    assertThat(approvedAt)
        .isEqualTo(OffsetDateTime.parse("2026-06-29T12:34:56+09:00"));
    assertThat(cancelledAt)
        .isEqualTo(OffsetDateTime.parse("2026-06-29T13:34:56+09:00"));
  }

  @Test
  void readJsonRejectsEmptyGatewayPayloadAsGatewayFailure() {
    TossPaymentGateway gateway = gateway();

    assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(
        gateway,
        "readJson",
        null,
        DomainExceptionCode.PAYMENT_APPROVAL_FAILED
    ))
        .isInstanceOfSatisfying(PaymentGatewayException.class, exception ->
            assertThat(exception.getExceptionCode()).isEqualTo(DomainExceptionCode.PAYMENT_APPROVAL_FAILED));
  }

  @Test
  void readJsonOrNullHandlesEmptyGatewayPayload() {
    TossPaymentGateway gateway = gateway();

    Object nullPayload = ReflectionTestUtils.invokeMethod(gateway, "readJsonOrNull", (String) null);
    Object blankPayload = ReflectionTestUtils.invokeMethod(gateway, "readJsonOrNull", " ");

    assertThat(nullPayload).isNull();
    assertThat(blankPayload).isNull();
  }

  @Test
  void treatsAlreadyCanceledAndRefundedCodesAsIdempotentSuccess() {
    TossPaymentGateway gateway = gateway();

    assertThat((Boolean) ReflectionTestUtils.invokeMethod(
        gateway, "isIdempotentlyCanceled", "ALREADY_CANCELED_PAYMENT")).isTrue();
    assertThat((Boolean) ReflectionTestUtils.invokeMethod(
        gateway, "isIdempotentlyCanceled", "ALREADY_REFUND_PAYMENT")).isTrue();
  }

  @Test
  void doesNotTreatGenuineCancelFailuresAsIdempotentSuccess() {
    TossPaymentGateway gateway = gateway();

    assertThat((Boolean) ReflectionTestUtils.invokeMethod(
        gateway, "isIdempotentlyCanceled", "NOT_CANCELABLE_PAYMENT")).isFalse();
    assertThat((Boolean) ReflectionTestUtils.invokeMethod(
        gateway, "isIdempotentlyCanceled", (String) null)).isFalse();
  }

  @Test
  void buildsCancelResultFromLatestCancelOfPaymentPayload() throws Exception {
    TossPaymentGateway gateway = gateway();
    JsonNode response = new ObjectMapper().readTree("""
        {
          "paymentKey": "test_payment_key",
          "status": "CANCELED",
          "cancels": [
            {"transactionKey": "txn_old", "cancelAmount": 10000, "canceledAt": "2026-06-29T10:00:00+09:00"},
            {"transactionKey": "txn_latest", "cancelAmount": 30000, "canceledAt": "2026-06-29T13:34:56+09:00"}
          ]
        }
        """);

    PaymentCancelResult result = ReflectionTestUtils.invokeMethod(
        gateway, "toCancelResult", response, "req-payload", "resp-payload");

    assertThat(result).isNotNull();
    assertThat(result.getPaymentKey()).isEqualTo("test_payment_key");
    assertThat(result.getPgStatus()).isEqualTo("CANCELED");
    assertThat(result.getPgCancellationKey()).isEqualTo("txn_latest");
    assertThat(result.getCancelAmount()).isEqualByComparingTo("30000");
    assertThat(result.getCancelledAt()).isEqualTo(OffsetDateTime.parse("2026-06-29T13:34:56+09:00"));
    assertThat(result.getRequestPayload()).isEqualTo("req-payload");
    assertThat(result.getResponsePayload()).isEqualTo("resp-payload");
  }

  private TossPaymentGateway gateway() {
    RestClient.Builder builder = org.mockito.Mockito.mock(RestClient.Builder.class);
    RestClient restClient = org.mockito.Mockito.mock(RestClient.class);
    TossPaymentProperties properties = new TossPaymentProperties();
    properties.setBaseUrl("https://example.com");
    properties.setSecretKey("secret");

    when(builder.baseUrl("https://example.com")).thenReturn(builder);
    when(builder.defaultHeaders(org.mockito.ArgumentMatchers.<Consumer<HttpHeaders>>any()))
        .thenReturn(builder);
    when(builder.requestFactory(any(ClientHttpRequestFactory.class))).thenReturn(builder);
    when(builder.build()).thenReturn(restClient);

    return new TossPaymentGateway(builder, properties, new ObjectMapper());
  }
}
