package com.healthdekho.repository;

import com.healthdekho.entity.PremiumInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PremiumInstallmentRepository
        extends JpaRepository<PremiumInstallment, Long> {

    List<PremiumInstallment>
    findByPolicy_PolicyIdOrderByInstallmentNumberAsc(Long policyId);
}