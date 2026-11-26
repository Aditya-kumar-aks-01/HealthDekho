package com.healthdekho.repository;

import com.healthdekho.entity.PremiumInstallment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PremiumInstallmentRepository
        extends JpaRepository<PremiumInstallment, Long> {

   @Query("""
        SELECT i
        FROM PremiumInstallment i
        JOIN i.policy p
        JOIN p.customer c
        WHERE i.installmentId = :installmentId
          AND c.email = :email
        """)
Optional<PremiumInstallment> findByIdAndCustomerEmail(
        @Param("installmentId") Long installmentId,
        @Param("email") String email
);

    List<PremiumInstallment> findByPolicy_PolicyIdOrderByInstallmentNumberAsc(
            Long policyId
    );
}