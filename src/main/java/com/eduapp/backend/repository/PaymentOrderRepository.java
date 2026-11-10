package com.eduapp.backend.repository;

import com.eduapp.backend.model.PaymentOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    Optional<PaymentOrder> findByOrderIdAndUserId(String orderId, Long userId);

    /** Row lock so two concurrent notifications for the same order are processed one at a time. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM PaymentOrder o WHERE o.orderId = :orderId")
    Optional<PaymentOrder> findByOrderIdForUpdate(@Param("orderId") String orderId);
}
