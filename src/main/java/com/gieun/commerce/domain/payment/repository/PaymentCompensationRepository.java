package com.gieun.commerce.domain.payment.repository;

import com.gieun.commerce.domain.payment.entity.CompensationStatus;
import com.gieun.commerce.domain.payment.entity.PaymentCompensation;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentCompensationRepository extends JpaRepository<PaymentCompensation, Long> {

  @Query("""
      select c
      from PaymentCompensation c
      where c.status = :status
        and c.nextRetryAt <= :now
      order by c.nextRetryAt asc
      """)
  List<PaymentCompensation> findDueForRetry(
      @Param("status") CompensationStatus status,
      @Param("now") LocalDateTime now,
      Pageable pageable
  );
}
