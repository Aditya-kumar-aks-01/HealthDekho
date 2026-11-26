package com.healthdekho.repository;

import com.healthdekho.entity.PremiumPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PremiumPaymentRepository
        extends JpaRepository<PremiumPayment, Long> {

    List<PremiumPayment> findByInstallment_InstallmentIdOrderByPaymentDateDesc(
            Long installmentId
    );

    @Query("""
            SELECT COALESCE(SUM(p.penaltyAmount), 0)
            FROM PremiumPayment p
            WHERE p.installment.installmentId = :installmentId
            """)
    BigDecimal calculateTotalPenalty(
            @Param("installmentId") Long installmentId
    );

    @Query("""
            SELECT p
            FROM PremiumPayment p
            WHERE p.installment.installmentId = :installmentId
              AND p.installment.policy.customer.email = :email
            ORDER BY p.paymentDate DESC
            """)
    List<PremiumPayment> findPaymentHistory(
            @Param("installmentId") Long installmentId,
            @Param("email") String email
    );
}