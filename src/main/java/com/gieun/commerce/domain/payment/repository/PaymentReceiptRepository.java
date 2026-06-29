package com.gieun.commerce.domain.payment.repository;

import com.gieun.commerce.domain.payment.entity.PaymentReceipt;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentReceiptRepository extends JpaRepository<PaymentReceipt, Long> {

  Optional<PaymentReceipt> findByPaymentId(Long paymentId);

  Optional<PaymentReceipt> findByPaymentKey(String paymentKey);
}
