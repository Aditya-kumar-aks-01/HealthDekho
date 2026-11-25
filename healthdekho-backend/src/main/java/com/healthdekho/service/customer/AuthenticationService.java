
package com.healthdekho.service.customer;

import com.healthdekho.dto.customer.AuthResponse;
import com.healthdekho.dto.customer.CustomerLoginRequest;
import com.healthdekho.security.JwtService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.healthdekho.exception.InvalidCredentialsException;
import org.springframework.security.authentication.BadCredentialsException;


@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationService(
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

   public AuthResponse login(CustomerLoginRequest request) {

    try {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.email(),
                                request.password()
                        )
                );

        org.springframework.security.core.userdetails.UserDetails userDetails =
                (org.springframework.security.core.userdetails.UserDetails)
                        authentication.getPrincipal();

        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds()
        );

    } catch (BadCredentialsException exception) {
        throw new InvalidCredentialsException(
                "Invalid email or password."
        );
    }
}
}
