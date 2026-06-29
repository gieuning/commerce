package com.gieun.commerce.domain.payment.repository;

import com.gieun.commerce.domain.payment.entity.PaymentCancellation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentCancellationRepository extends JpaRepository<PaymentCancellation, Long> {

  List<PaymentCancellation> findByPaymentIdOrderByCancelledAtDesc(Long paymentId);
}
