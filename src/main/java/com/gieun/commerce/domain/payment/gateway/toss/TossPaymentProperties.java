package com.gieun.commerce.domain.payment.gateway.toss;

import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "payment.toss")
public class TossPaymentProperties {

  private String baseUrl = "https://api.tosspayments.com";
  @NotBlank
  private String secretKey;
  private Duration connectTimeout = Duration.ofSeconds(3);
  private Duration readTimeout = Duration.ofSeconds(10);
}
