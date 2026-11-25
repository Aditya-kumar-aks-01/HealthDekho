
package com.healthdekho.service.customer;

import com.healthdekho.entity.Customer;
import com.healthdekho.repository.CustomerRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomerDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    public CustomerDetailsService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Customer not found")
                );

        return User.builder()
                .username(customer.getEmail())
                .password(customer.getPasswordHash())
                .roles("CUSTOMER")
                .disabled(!"ACTIVE".equalsIgnoreCase(customer.getStatus()))
                .build();
    }
}
