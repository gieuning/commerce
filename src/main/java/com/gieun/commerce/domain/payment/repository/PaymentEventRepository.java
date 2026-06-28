package com.gieun.commerce.domain.payment.repository;

import com.gieun.commerce.domain.payment.entity.PaymentEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentEventRepository extends JpaRepository<PaymentEvent, Long> {

  List<PaymentEvent> findByPaymentIdOrderByOccurredAtAsc(Long paymentId);

  List<PaymentEvent> findByPaymentIdOrderByOccurredAtDesc(Long paymentId);
}
