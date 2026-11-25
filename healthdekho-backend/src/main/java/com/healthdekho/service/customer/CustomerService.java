package com.healthdekho.service.customer;

import com.healthdekho.dto.customer.CustomerRegistrationRequest;
import com.healthdekho.dto.customer.CustomerResponse;
import com.healthdekho.entity.Customer;
import com.healthdekho.exception.EmailAlreadyExistsException;
import com.healthdekho.repository.CustomerRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.healthdekho.exception.CustomerNotFoundException;
import java.util.Optional;
import java.time.OffsetDateTime;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CustomerResponse registerCustomer(
            CustomerRegistrationRequest request
    ) {

        // 1. Check whether email already exists
        if (customerRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(
                    "An account with this email already exists."
            );
        }

        // 2. Hash the password
        String hashedPassword = passwordEncoder.encode(request.password());

        // 3. Create customer entity
        Customer customer = new Customer(
                request.email(),
                hashedPassword,
                request.fullName(),
                request.dateOfBirth(),
                request.phone()
        );

        // 4. Set audit timestamps
        OffsetDateTime now = OffsetDateTime.now();

        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);

        // 5. Save customer
        Customer savedCustomer = customerRepository.save(customer);

        // 6. Convert entity into response DTO
        return new CustomerResponse(
                savedCustomer.getCustomerId(),
                savedCustomer.getFullName(),
                savedCustomer.getEmail(),
                savedCustomer.getDateOfBirth(),
                savedCustomer.getPhone(),
                savedCustomer.getStatus(),
                savedCustomer.getCreatedAt()
        );
    }

@Transactional(readOnly = true)
public CustomerResponse getCustomerProfile(String email) {

    Customer customer = customerRepository.findByEmail(email)
            .orElseThrow(() ->
                    new CustomerNotFoundException(
                            "Customer profile not found."
                    )
            );

    return new CustomerResponse(
            customer.getCustomerId(),
            customer.getFullName(),
            customer.getEmail(),
            customer.getDateOfBirth(),
            customer.getPhone(),
            customer.getStatus(),
            customer.getCreatedAt()
    );
}

}