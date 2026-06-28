package com.gieun.commerce.domain.payment.repository;

import com.gieun.commerce.domain.payment.entity.Payment;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

  Optional<Payment> findByIdAndUserId(Long id, Long userId);

  Optional<Payment> findByMerchantOrderIdAndUserId(String merchantOrderId, Long userId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
    select p
    from Payment p
    where p.id = :id
      and p.userId = :userId
    """)
  Optional<Payment> findByIdAndUserIdForUpdate(@Param("id") Long id, @Param("userId") Long userId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Payment p where p.orderId = :orderId and p.userId = :userId")
  Optional<Payment> findByOrderIdAndUserIdForUpdate(@Param("orderId") Long orderId, @Param("userId") Long userId);
}
