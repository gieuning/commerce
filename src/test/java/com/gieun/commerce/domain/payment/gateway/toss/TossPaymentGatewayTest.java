package com.gieun.commerce.domain.payment.gateway.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
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

    when(builder.baseUrl(eq("https://example.com"))).thenReturn(builder);
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
}
