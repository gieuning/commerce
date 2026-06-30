package com.gieun.commerce.domain.payment.entity;

public enum CompensationStatus {
  PENDING,  // 환불 재시도 대기
  DONE,     // 환불 완료
  GAVE_UP   // 최대 재시도 초과 — 수동 처리 필요
}
