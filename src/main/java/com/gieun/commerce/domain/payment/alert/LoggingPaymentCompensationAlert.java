package com.gieun.commerce.domain.payment.alert;

import com.gieun.commerce.domain.payment.entity.PaymentCompensation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 기본 알림 구현: ERROR 로그로 남긴다. GAVE_UP은 PG에 결제가 잡힌 채 방치되어 수동 처리가 필요한 상태다.
 */
@Component
public class LoggingPaymentCompensationAlert implements PaymentCompensationAlert {

  private static final Logger log = LoggerFactory.getLogger(LoggingPaymentCompensationAlert.class);

  @Override
  public void giveUp(PaymentCompensation compensation) {
    log.error(
        "[ALERT] 결제 보상 환불 최종 실패(GAVE_UP) — PG에 결제가 잡힌 채 방치됨, 수동 처리 필요. "
            + "compensationId={}, paymentId={}, paymentKey={}, cancelAmount={}, attempts={}, lastError={}",
        compensation.getId(),
        compensation.getPaymentId(),
        compensation.getPaymentKey(),
        compensation.getCancelAmount(),
        compensation.getAttemptCount(),
        compensation.getLastError()
    );
  }
}
