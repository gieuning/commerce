package com.gieun.commerce.domain.payment.alert;

import com.gieun.commerce.domain.payment.entity.PaymentCompensation;

/**
 * 결제 보상(환불) 재시도가 최종 실패(GAVE_UP)했을 때 운영자에게 알리는 훅.
 * 기본 구현은 로그만 남기며, 추후 Slack/메일 등 어댑터로 교체할 수 있다.
 */
public interface PaymentCompensationAlert {

  void giveUp(PaymentCompensation compensation);
}
