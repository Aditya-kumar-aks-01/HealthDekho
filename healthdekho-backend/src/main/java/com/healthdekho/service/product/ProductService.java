
package com.healthdekho.service.product;

import com.healthdekho.dto.product.InsuranceProductResponse;
import com.healthdekho.entity.CustomerPolicy;
import com.healthdekho.entity.InsuranceProduct;
import com.healthdekho.exception.ProductNotFoundException;
import com.healthdekho.repository.CustomerPolicyRepository;
import com.healthdekho.repository.InsuranceProductRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthdekho.repository.CustomerRepository;
import com.healthdekho.exception.CustomerNotFoundException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final InsuranceProductRepository productRepository;
    private final CustomerPolicyRepository customerPolicyRepository;
    private final CustomerRepository customerRepository;

    public ProductService(
        InsuranceProductRepository productRepository,
        CustomerPolicyRepository customerPolicyRepository,
        CustomerRepository customerRepository
) {
    this.productRepository = productRepository;
    this.customerPolicyRepository = customerPolicyRepository;
    this.customerRepository = customerRepository;
}

    // Retrieve all active insurance products
    @Transactional(readOnly = true)
    public List<InsuranceProductResponse> getAllActiveProducts() {

        return productRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Retrieve one active insurance product
    @Transactional(readOnly = true)
    public InsuranceProductResponse getProductById(Long productId) {

        InsuranceProduct product = productRepository.findById(productId)
                .filter(InsuranceProduct::getActive)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Active insurance product not found."
                        )
                );

        return mapToResponse(product);
    }

    // Retrieve active products not already registered by the customer
    @Transactional(readOnly = true)
public List<InsuranceProductResponse> getAvailableProducts(String email) {

    Long customerId = customerRepository.findByEmail(email)
            .orElseThrow(() ->
                    new CustomerNotFoundException("Customer not found.")
            )
            .getCustomerId();

    List<CustomerPolicy> customerPolicies =
            customerPolicyRepository.findByCustomer_CustomerId(customerId);

    Set<Long> ownedProductIds = customerPolicies.stream()
            .map(policy -> policy.getProduct().getProductId())
            .collect(Collectors.toSet());

    return productRepository.findByActiveTrue()
            .stream()
            .filter(product ->
                    !ownedProductIds.contains(product.getProductId())
            )
            .map(this::mapToResponse)
            .toList();
}

    // Convert entity into response DTO
    private InsuranceProductResponse mapToResponse(
            InsuranceProduct product
    ) {

        return new InsuranceProductResponse(
                product.getProductId(),
                product.getProductCode(),
                product.getPolicyName(),
                product.getPolicyType(),
                product.getTermMonths(),
                product.getPremiumFrequency(),
                product.getPremiumAmount(),
                product.getSumInsured(),
                product.getMinEntryAge(),
                product.getMaxEntryAge(),
                product.getActive()
        );
    }
}
