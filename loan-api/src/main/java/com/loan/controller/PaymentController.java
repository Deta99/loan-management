package com.loan.controller;

import com.loan.dto.request.PaymentRequest;
import com.loan.dto.response.PaymentResponse;
import com.loan.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment management endpoints")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/loan/{loanId}")
    @Operation(summary = "Make a payment on an approved loan — USER only")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentResponse> makePayment(
            @PathVariable Long loanId,
            @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(paymentService.makePayment(loanId, request));
    }

    @GetMapping("/loan/{loanId}")
    @Operation(summary = "Get all payments for a loan — USER and ADMIN")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(paymentService.getPaymentsByLoan(loanId));
    }
}