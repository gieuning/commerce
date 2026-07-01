package com.gieun.commerce.domain.payment.service;

import com.gieun.commerce.domain.payment.alert.PaymentCompensationAlert;
import com.gieun.commerce.domain.payment.entity.CompensationStatus;
import com.gieun.commerce.domain.payment.entity.PaymentCompensation;
import com.gieun.commerce.domain.payment.gateway.PaymentCancelCommand;
import com.gieun.commerce.domain.payment.gateway.PaymentGateway;
import com.gieun.commerce.domain.payment.gateway.PaymentGatewayException;
import com.gieun.commerce.domain.payment.repository.PaymentCompensationRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * PENDING 상태의 보상(환불) 건을 PG에 재시도한다. 외부 PG 호출은 트랜잭션 밖에서 수행하고,
 * 결과 반영(완료/실패 카운트 증가)만 짧은 트랜잭션으로 처리한다. (confirm/cancel과 동일한 분리 원칙)
 */
@Service
@RequiredArgsConstructor
public class PaymentCompensationService {

  private static final int BATCH_SIZE = 50;
  private static final Logger log = LoggerFactory.getLogger(PaymentCompensationService.class);

  private final PaymentCompensationRepository compensationRepository;
  private final PaymentGateway paymentGateway;
  private final TransactionTemplate transactionTemplate;
  private final PaymentCompensationAlert compensationAlert;

  /** 재시도 대상 건들을 처리하고 처리 건수를 반환한다. */
  public int retryPendingCompensations() {
    List<PaymentCompensation> due = compensationRepository.findDueForRetry(
        CompensationStatus.PENDING,
        LocalDateTime.now(),
        PageRequest.of(0, BATCH_SIZE)
    );

    int processed = 0;
    for (PaymentCompensation compensation : due) {
      retryOne(compensation.getId(), compensation.getPaymentKey(),
          compensation.getCancelAmount(), compensation.getReason());
      processed++;
    }
    return processed;
  }

  private void retryOne(Long id, String paymentKey, BigDecimal cancelAmount, String reason) {
    try {
      paymentGateway.cancel(
          PaymentCancelCommand.builder()
              .paymentKey(paymentKey)
              .cancelAmount(cancelAmount)
              .cancelReason(reason)
              .build()
      );
    } catch (PaymentGatewayException exception) {
      String error = exception.getPgCode() + ": " + exception.getPgMessage();
      AtomicReference<PaymentCompensation> updatedRef = new AtomicReference<>();
      transactionTemplate.executeWithoutResult(status ->
          compensationRepository.findById(id).ifPresent(c -> {
            c.recordFailure(error, LocalDateTime.now());
            updatedRef.set(c);
          }));

      PaymentCompensation updated = updatedRef.get();
      if (updated == null) {
        return;
      }
      if (updated.getStatus() == CompensationStatus.GAVE_UP) {
        // 최대 재시도 초과 → PG에 돈이 잡힌 채 방치. 운영자 알림 훅 호출.
        compensationAlert.giveUp(updated);
      } else {
        log.warn("결제 보상 환불 재시도 실패 — compensationId={}, paymentKey={}, attempt={}/{}, nextRetryAt={}, error={}",
            updated.getId(), updated.getPaymentKey(), updated.getAttemptCount(),
            updated.getMaxAttempts(), updated.getNextRetryAt(), error);
      }
      return;
    }

    transactionTemplate.executeWithoutResult(status ->
        compensationRepository.findById(id).ifPresent(PaymentCompensation::markDone));
  }
}
