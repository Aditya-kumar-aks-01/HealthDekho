package com.healthdekho.repository;

import com.healthdekho.entity.CustomerPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
public interface CustomerPolicyRepository
        extends JpaRepository<CustomerPolicy, Long> {

    Optional<CustomerPolicy> findByPolicyNumber(String policyNumber);

    boolean existsByCustomer_CustomerIdAndProduct_ProductId(
            Long customerId, Long productId);

    List<CustomerPolicy> findByCustomer_CustomerId(Long customerId);
}