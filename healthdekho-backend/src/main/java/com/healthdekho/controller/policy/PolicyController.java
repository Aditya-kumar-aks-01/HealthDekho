
package com.healthdekho.controller.policy;

import com.healthdekho.dto.policy.PolicyRegistrationRequest;
import com.healthdekho.dto.policy.PolicyRegistrationResponse;
import com.healthdekho.service.policy.PolicyService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping("/register")
    public ResponseEntity<PolicyRegistrationResponse> registerPolicy(
            @Valid @RequestBody PolicyRegistrationRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        PolicyRegistrationResponse response =
                policyService.registerPolicy(
                        userDetails.getUsername(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
