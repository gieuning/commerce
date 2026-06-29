package com.gieun.commerce.domain.payment.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gieun.commerce.global.exception.DomainException;
import com.gieun.commerce.global.exception.DomainExceptionCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class PaymentTest {

  @Test
  void approveOnlyRequestedPayment() {
    Payment payment = requestedPayment();
    payment.fail("PG_ERROR", "승인 실패");

    assertThatThrownBy(() -> payment.approve("payment_key", LocalDateTime.now()))
        .isInstanceOfSatisfying(DomainException.class, exception ->
            assertThat(exception.getCode()).isEqualTo(DomainExceptionCode.CANNOT_CONFIRM_PAYMENT.name()));
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
  }

  @Test
  void failOnlyRequestedPayment() {
    Payment payment = requestedPayment();
    payment.approve("payment_key", LocalDateTime.now());

    assertThatThrownBy(() -> payment.fail("PG_ERROR", "승인 실패"))
        .isInstanceOfSatisfying(DomainException.class, exception ->
            assertThat(exception.getCode()).isEqualTo(DomainExceptionCode.CANNOT_CONFIRM_PAYMENT.name()));
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
  }

  @Test
  void cancelOnlyApprovedPayment() {
    Payment payment = requestedPayment();

    assertThatThrownBy(() -> payment.cancel(LocalDateTime.now()))
        .isInstanceOfSatisfying(DomainException.class, exception ->
            assertThat(exception.getCode()).isEqualTo(DomainExceptionCode.CANNOT_CANCEL_PAYMENT.name()));
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REQUESTED);
  }

  @Test
  void requestRejectsNullAmountWithDomainException() {
    assertThatThrownBy(() -> requestPayment(null))
        .isInstanceOfSatisfying(DomainException.class, exception ->
            assertThat(exception.getCode()).isEqualTo(DomainExceptionCode.INVALID_PAYMENT_AMOUNT.name()));
  }

  @Test
  void requestRejectsNonPositiveAmountWithDomainException() {
    assertThatThrownBy(() -> requestPayment(BigDecimal.ZERO))
        .isInstanceOfSatisfying(DomainException.class, exception ->
            assertThat(exception.getCode()).isEqualTo(DomainExceptionCode.INVALID_PAYMENT_AMOUNT.name()));
  }

  private Payment requestedPayment() {
    return requestPayment(new BigDecimal("30000.00"));
  }

  private Payment requestPayment(BigDecimal amount) {
    return Payment.request(
        10L,
        1L,
        "20260628000010ABCDEF123456",
        PgProvider.TOSS,
        PaymentMethod.CARD,
        amount
    );
  }
}
