package com.spendsmart.payment.repository;

import com.spendsmart.payment.entity.PremiumPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PremiumPaymentRepository extends JpaRepository<PremiumPayment, Long> {
    Optional<PremiumPayment> findByUserEmail(String userEmail);
    boolean existsByUserEmail(String userEmail);
}
