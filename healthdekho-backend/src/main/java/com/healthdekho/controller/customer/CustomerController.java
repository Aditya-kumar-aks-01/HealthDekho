
package com.healthdekho.controller.customer;

import com.healthdekho.dto.customer.CustomerRegistrationRequest;
import com.healthdekho.dto.customer.CustomerResponse;
import com.healthdekho.dto.customer.CustomerLoginRequest;
import com.healthdekho.dto.customer.AuthResponse;

import com.healthdekho.service.customer.CustomerService;
import com.healthdekho.service.customer.AuthenticationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final AuthenticationService authenticationService;

    public CustomerController(
            CustomerService customerService,
            AuthenticationService authenticationService
    ) {
        this.customerService = customerService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<CustomerResponse> registerCustomer(
            @Valid @RequestBody CustomerRegistrationRequest request
    ) {

        CustomerResponse response =
                customerService.registerCustomer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody CustomerLoginRequest request
    ) {

        AuthResponse response =
                authenticationService.login(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<CustomerResponse> getCustomerProfile(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CustomerResponse response =
                customerService.getCustomerProfile(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

@GetMapping("/me")
public ResponseEntity<CustomerResponse> getMyProfile(
        @AuthenticationPrincipal UserDetails userDetails
) {

    CustomerResponse response =
            customerService.getCustomerProfile(
                    userDetails.getUsername()
            );

    return ResponseEntity.ok(response);
}
}
