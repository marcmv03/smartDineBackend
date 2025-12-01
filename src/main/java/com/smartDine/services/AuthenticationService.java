// src/main/java/com/smartDine/services/AuthenticationService.java

package com.smartDine.services;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.smartDine.dto.auth.LoginRequest;
import com.smartDine.dto.auth.RegisterBusinessRequest;
import com.smartDine.dto.auth.RegisterCustomerRequest;
import com.smartDine.entity.Business;
import com.smartDine.entity.Customer;
import com.smartDine.entity.User;
import com.smartDine.exceptions.DuplicateUserException;
import com.smartDine.repository.BusinessRepository;
import com.smartDine.repository.CustomerRepository;
import com.smartDine.repository.UserRepository;

@Service
public class AuthenticationService {
    private final BusinessRepository businessRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
        BusinessRepository businessRepository,
        CustomerRepository customerRepository,
        UserRepository userRepository,
        AuthenticationManager authenticationManager,
        PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.businessRepository = businessRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    /**
     * Registers a new Customer user.
     */
    public Customer registerCustomer(RegisterCustomerRequest request) {
        try {
            Customer user = new Customer();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setPhoneNumber(request.getPhoneNumber());
            user.setRole("customer");
            return customerRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw DuplicateUserException.fromDataIntegrityViolation(e);
        }
    }
    

    /**
     * Authenticates a Customer user.
     */
   public User authenticate(LoginRequest input) {
    try {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );

        return userRepository.findByEmail(input.getEmail())
                .orElseThrow();
    }
    catch(AuthenticationServiceException e) {
        throw new BadCredentialsException("Invalid credentials");
    }
}
    /**
     * Registers a new Business user.
     */
    public Business registerBusiness(RegisterBusinessRequest request) {
        try {
            Business user = new Business();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setPhoneNumber(request.getPhoneNumber());
            user.setRole("business");

            return businessRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw DuplicateUserException.fromDataIntegrityViolation(e);
        }
    }
}