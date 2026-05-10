package com.loan.service;

import com.loan.dto.event.LoanApprovedEvent;
import com.loan.dto.request.LoanRequest;
import com.loan.dto.response.LoanResponse;
import com.loan.entity.Customer;
import com.loan.entity.Loan;
import com.loan.exception.LoanNotPendingException;
import com.loan.repository.CustomerRepository;
import com.loan.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.loan.exception.ResourceNotFoundException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {

    private final LoanRepository loanRepository;
    private final CustomerRepository customerRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String LOAN_APPROVED_TOPIC = "loan-approved";
    @Transactional
    public LoanResponse applyForLoan(Long customerId, LoanRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        Loan loan = new Loan();
        loan.setAmount(request.getAmount());
        loan.setInterestRate(request.getInterestRate());
        loan.setTermMonths(request.getTermMonths());
        loan.setCustomer(customer);

        Loan saved = loanRepository.save(loan);
        log.info("Loan applied for customer: {}", customerId);

        return mapToResponse(saved);
    }

    @Transactional
    public LoanResponse approveLoan(Long loanId) {
    Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + loanId));

    if (!loan.getStatus().equals("PENDING")) {
        throw new LoanNotPendingException("Loan is not in PENDING state — current status: " + loan.getStatus());
    }

    loan.setStatus("APPROVED");
    Loan saved = loanRepository.save(loan);
    log.info("Loan approved: {}", loanId);

    // Publish Kafka event
    LoanApprovedEvent event = new LoanApprovedEvent(
            saved.getId(),
            saved.getCustomer().getId(),
            saved.getCustomer().getFullName(),
            saved.getCustomer().getEmail(),
            saved.getAmount(),
            saved.getTermMonths()
    );

    kafkaTemplate.send(LOAN_APPROVED_TOPIC, String.valueOf(loanId), event);
    log.info("Loan approved event published for loan id: {}", loanId);

    return mapToResponse(saved);
}

    @Transactional
    public LoanResponse rejectLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + loanId));

        // Can only approve PENDING loans
        if (!loan.getStatus().equals("PENDING")) {
            throw new LoanNotPendingException("Loan is not in PENDING state — current status: " + loan.getStatus());
        }
        
        loan.setStatus("REJECTED");
        Loan saved = loanRepository.save(loan);
        log.info("Loan rejected: {}", loanId);

        return mapToResponse(saved);
    }
    
    @Transactional(readOnly = true)
    public List<LoanResponse> getLoansByCustomer(Long customerId) {
        return loanRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> getLoansByStatus(String status) {
        return loanRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private LoanResponse mapToResponse(Loan loan) {
        LoanResponse response = new LoanResponse();
        response.setId(loan.getId());
        response.setAmount(loan.getAmount());
        response.setInterestRate(loan.getInterestRate());
        response.setTermMonths(loan.getTermMonths());
        response.setStatus(loan.getStatus());
        response.setCreatedAt(loan.getCreatedAt());
        response.setCustomerName(loan.getCustomer().getFullName());
        return response;
    }
}