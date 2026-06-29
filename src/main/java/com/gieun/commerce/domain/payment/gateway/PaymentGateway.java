package com.gieun.commerce.domain.payment.gateway;

public interface PaymentGateway {

  PaymentConfirmResult confirm(PaymentConfirmCommand command);

  PaymentCancelResult cancel(PaymentCancelCommand command);
}
