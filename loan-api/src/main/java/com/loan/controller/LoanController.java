package com.loan.controller;

import com.loan.dto.request.LoanRequest;
import com.loan.dto.response.LoanResponse;
import com.loan.service.LoanService;
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
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Tag(name = "Loans", description = "Loan management endpoints")
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/customer/{customerId}")
    @Operation(summary = "Apply for a loan — USER only")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<LoanResponse> applyForLoan(
            @PathVariable Long customerId,
            @Valid @RequestBody LoanRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(loanService.applyForLoan(customerId, request));
    }

    @PutMapping("/{loanId}/approve")
    @Operation(summary = "Approve a loan — ADMIN only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanResponse> approveLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.approveLoan(loanId));
    }

    @PutMapping("/{loanId}/reject")
    @Operation(summary = "Reject a loan — ADMIN only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanResponse> rejectLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.rejectLoan(loanId));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get loans by customer — USER and ADMIN")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<LoanResponse>> getLoansByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(loanService.getLoansByCustomer(customerId));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get loans by status — ADMIN only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LoanResponse>> getLoansByStatus(@PathVariable String status) {
        return ResponseEntity.ok(loanService.getLoansByStatus(status));
    }
}