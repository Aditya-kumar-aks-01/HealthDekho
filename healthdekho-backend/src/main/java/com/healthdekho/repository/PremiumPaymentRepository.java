package com.healthdekho.repository;

import com.healthdekho.entity.PremiumPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PremiumPaymentRepository
        extends JpaRepository<PremiumPayment, Long> {

    List<PremiumPayment>
    findByInstallment_InstallmentIdOrderByPaymentDateDesc(Long installmentId);

    Optional<PremiumPayment> findByTransactionReference(String transactionReference);
}