package com.gieun.commerce.domain.payment.gateway;

import com.gieun.commerce.global.exception.DomainExceptionCode;
import lombok.Getter;

@Getter
public class PaymentGatewayException extends RuntimeException {

  private final DomainExceptionCode exceptionCode;
  private final String pgCode;
  private final String pgMessage;
  private final String responsePayload;

  public PaymentGatewayException(
      DomainExceptionCode exceptionCode,
      String pgCode,
      String pgMessage,
      String responsePayload
  ) {
    super(pgMessage);
    this.exceptionCode = exceptionCode;
    this.pgCode = pgCode;
    this.pgMessage = pgMessage;
    this.responsePayload = responsePayload;
  }
}
