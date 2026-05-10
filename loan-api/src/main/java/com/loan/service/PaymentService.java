package com.loan.service;

import com.loan.dto.request.PaymentRequest;
import com.loan.dto.response.PaymentResponse;
import com.loan.entity.Loan;
import com.loan.entity.Payment;
import com.loan.exception.LoanNotPendingException;
import com.loan.exception.ResourceNotFoundException;
import com.loan.repository.LoanRepository;
import com.loan.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final LoanRepository loanRepository;

    @Transactional
    public PaymentResponse makePayment(Long loanId, PaymentRequest request) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + loanId));

        // Can only make payments on APPROVED loans
        if (!loan.getStatus().equals("APPROVED")) {
            throw new LoanNotPendingException("Cannot make payment — loan status is: " + loan.getStatus());
        }

        Payment payment = new Payment();
        payment.setAmount(request.getAmount());
        payment.setLoan(loan);

        Payment saved = paymentRepository.save(payment);
        log.info("Payment made for loan id: {}", loanId);

        return mapToResponse(saved);
    }
    
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByLoan(Long loanId) {
        if (!loanRepository.existsById(loanId)) {
            throw new ResourceNotFoundException("Loan not found with id: " + loanId);
        }
        return paymentRepository.findByLoanId(loanId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setAmount(payment.getAmount());
        response.setPaidAt(payment.getPaidAt());
        response.setLoanId(payment.getLoan().getId());
        return response;
    }
}