package com.gieun.commerce.domain.payment.scheduler;

import com.gieun.commerce.domain.payment.service.PaymentCompensationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentCompensationScheduler {

  private final PaymentCompensationService paymentCompensationService;

  // 기본 1분 주기. 직전 실행 완료 후 간격을 보장하기 위해 fixedDelay 사용.
  @Scheduled(fixedDelayString = "${payment.compensation.retry-interval-ms:60000}")
  public void retryPendingCompensations() {
    paymentCompensationService.retryPendingCompensations();
  }
}
