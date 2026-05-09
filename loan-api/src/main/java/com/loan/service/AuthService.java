package com.loan.service;

import com.loan.dto.request.AuthRequest;
import com.loan.dto.request.CustomerRequest;
import com.loan.dto.response.AuthResponse;
import com.loan.entity.Customer;
import com.loan.exception.EmailAlreadyExistsException;
import com.loan.repository.CustomerRepository;
import com.loan.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(CustomerRequest request) {
        // Check if email already exists
        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        // Create customer with hashed password
        Customer customer = new Customer();
        customer.setFullName(request.getFullName());
        customer.setEmail(request.getEmail());
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setRole("USER"); // default role

        customerRepository.save(customer);
        log.info("New customer registered: {}", request.getEmail());

        // Generate and return token
        String token = jwtService.generateToken(customer.getEmail(), customer.getRole());
        return new AuthResponse(token, customer.getRole(), customer.getEmail());
    }

    public AuthResponse login(AuthRequest request) {
        // Authenticate — throws exception if wrong credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        // Load customer and generate token
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("Customer logged in: {}", request.getEmail());

        String token = jwtService.generateToken(customer.getEmail(), customer.getRole());
        return new AuthResponse(token, customer.getRole(), customer.getEmail());
    }
}