package com.gieun.commerce.domain.payment.gateway.toss;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class TossPaymentPropertiesTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
      .withUserConfiguration(TossPaymentConfig.class);

  @Test
  void failsWhenSecretKeyIsBlank() {
    contextRunner
        .withPropertyValues("payment.toss.secret-key=")
        .run(context -> assertThat(context).hasFailed());
  }

  @Test
  void bindsWhenSecretKeyExists() {
    contextRunner
        .withPropertyValues("payment.toss.secret-key=test_secret")
        .run(context -> {
          assertThat(context).hasNotFailed();
          assertThat(context.getBean(TossPaymentProperties.class).getSecretKey())
              .isEqualTo("test_secret");
        });
  }
}
