package com.gieun.commerce.domain.payment.entity;

public enum PaymentEventType {
  REQUESTED,
  APPROVED,
  FAILED,
  CANCEL_REQUESTED,
  CANCELLED,
  WEBHOOK_RECEIVED
}
