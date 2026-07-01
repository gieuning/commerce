package com.gieun.commerce.domain.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gieun.commerce.domain.payment.entity.CompensationStatus;
import com.gieun.commerce.domain.payment.entity.PaymentCompensation;
import com.gieun.commerce.domain.payment.entity.PgProvider;
import com.gieun.commerce.domain.payment.gateway.PaymentGateway;
import com.gieun.commerce.domain.payment.gateway.PaymentGatewayException;
import com.gieun.commerce.domain.payment.repository.PaymentCompensationRepository;
import com.gieun.commerce.global.exception.DomainExceptionCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class PaymentCompensationServiceTest {

  @Mock
  PaymentCompensationRepository compensationRepository;

  @Mock
  PaymentGateway paymentGateway;

  @Mock
  TransactionTemplate transactionTemplate;

  @InjectMocks
  PaymentCompensationService service;

  @BeforeEach
  void setUp() {
    // executeWithoutResult(Consumer) 의 콜백을 실제로 실행시키는 stub
    lenient().doAnswer(invocation -> {
      Consumer<TransactionStatus> callback = invocation.getArgument(0);
      callback.accept(mock(TransactionStatus.class));
      return null;
    }).when(transactionTemplate).executeWithoutResult(any());
  }

  @Test
  void retrySuccessMarksCompensationDone() {
    PaymentCompensation compensation = pendingCompensation(1);
    stubDue(compensation);
    // 환불 성공: 기본 mock이 예외 없이 반환

    service.retryPendingCompensations();

    assertThat(compensation.getStatus()).isEqualTo(CompensationStatus.DONE);
    assertThat(compensation.getNextRetryAt()).isNull();
  }

  @Test
  void retryFailureIncrementsAttemptAndSchedulesBackoff() {
    PaymentCompensation compensation = pendingCompensation(1);
    stubDue(compensation);
    when(paymentGateway.cancel(any())).thenThrow(gatewayException());

    service.retryPendingCompensations();

    assertThat(compensation.getStatus()).isEqualTo(CompensationStatus.PENDING);
    assertThat(compensation.getAttemptCount()).isEqualTo(2);
    assertThat(compensation.getNextRetryAt()).isNotNull();
    assertThat(compensation.getLastError()).contains("PG_CANCEL_ERR");
  }

  @Test
  void reachingMaxAttemptsGivesUp() {
    // 마지막 시도(9회 완료) → 이번 실패로 10회 도달 → GAVE_UP
    PaymentCompensation compensation = pendingCompensation(PaymentCompensation.DEFAULT_MAX_ATTEMPTS - 1);
    stubDue(compensation);
    when(paymentGateway.cancel(any())).thenThrow(gatewayException());

    service.retryPendingCompensations();

    assertThat(compensation.getStatus()).isEqualTo(CompensationStatus.GAVE_UP);
    assertThat(compensation.getNextRetryAt()).isNull();
  }

  private PaymentCompensation pendingCompensation(int attemptCount) {
    PaymentCompensation compensation = PaymentCompensation.of(
        PaymentCompensation.CreateCommand.builder()
            .paymentId(1L)
            .pgProvider(PgProvider.TOSS)
            .paymentKey("pay_test_key")
            .cancelAmount(new BigDecimal("30000.00"))
            .reason("로컬 결제 승인 검증 실패")
            .status(CompensationStatus.PENDING)
            .attemptCount(attemptCount)
            .maxAttempts(PaymentCompensation.DEFAULT_MAX_ATTEMPTS)
            .nextRetryAt(LocalDateTime.now().minusMinutes(1))
            .build()
    );
    ReflectionTestUtils.setField(compensation, "id", 1L);
    return compensation;
  }

  private void stubDue(PaymentCompensation compensation) {
    when(compensationRepository.findDueForRetry(eq(CompensationStatus.PENDING), any(), any()))
        .thenReturn(List.of(compensation));
    when(compensationRepository.findById(1L)).thenReturn(Optional.of(compensation));
  }

  private PaymentGatewayException gatewayException() {
    return new PaymentGatewayException(
        DomainExceptionCode.PAYMENT_CANCEL_FAILED, "PG_CANCEL_ERR", "취소 실패",
        "{\"code\":\"PG_CANCEL_ERR\"}");
  }
}
