package com.loan.service;

import com.loan.dto.request.LoanRequest;
import com.loan.dto.response.LoanResponse;
import com.loan.entity.Customer;
import com.loan.entity.Loan;
import com.loan.exception.LoanNotPendingException;
import com.loan.exception.ResourceNotFoundException;
import com.loan.repository.CustomerRepository;
import com.loan.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private LoanService loanService;

    private Customer customer;
    private Loan pendingLoan;
    private LoanRequest loanRequest;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setFullName("Amer Zaatari");
        customer.setEmail("amer@gmail.com");
        customer.setRole("USER");

        pendingLoan = new Loan();
        pendingLoan.setId(1L);
        pendingLoan.setAmount(BigDecimal.valueOf(5000));
        pendingLoan.setInterestRate(BigDecimal.valueOf(5.5));
        pendingLoan.setTermMonths(12);
        pendingLoan.setStatus("PENDING");
        pendingLoan.setCustomer(customer);

        loanRequest = new LoanRequest();
        loanRequest.setAmount(BigDecimal.valueOf(5000));
        loanRequest.setInterestRate(BigDecimal.valueOf(5.5));
        loanRequest.setTermMonths(12);
    }

    @Test
    void shouldApplyForLoanSuccessfully() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(loanRepository.save(any(Loan.class))).thenReturn(pendingLoan);

        // Act
        LoanResponse response = loanService.applyForLoan(1L, loanRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getCustomerName()).isEqualTo("Amer Zaatari");

        verify(customerRepository, times(1)).findById(1L);
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    void shouldThrowWhenCustomerNotFoundOnLoanApply() {
        // Arrange
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> loanService.applyForLoan(99L, loanRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer not found with id: 99");

        verify(loanRepository, never()).save(any());
    }

    @Test
    void shouldApproveLoanAndPublishKafkaEvent() {
        // Arrange
        when(loanRepository.findById(1L)).thenReturn(Optional.of(pendingLoan));
        when(loanRepository.save(any(Loan.class))).thenReturn(pendingLoan);

        // Act
        LoanResponse response = loanService.approveLoan(1L);

        // Assert
        assertThat(response).isNotNull();
        verify(loanRepository, times(1)).save(any(Loan.class));
        verify(kafkaTemplate, times(1)).send(eq("loan-approved"), any(), any());
    }

    @Test
    void shouldThrowWhenApprovingNonPendingLoan() {
        // Arrange
        pendingLoan.setStatus("APPROVED");
        when(loanRepository.findById(1L)).thenReturn(Optional.of(pendingLoan));

        // Act & Assert
        assertThatThrownBy(() -> loanService.approveLoan(1L))
                .isInstanceOf(LoanNotPendingException.class)
                .hasMessageContaining("APPROVED");

        verify(loanRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void shouldThrowWhenLoanNotFoundOnApprove() {
        // Arrange
        when(loanRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> loanService.approveLoan(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Loan not found with id: 99");
    }

    @Test
    void shouldRejectLoanSuccessfully() {
        // Arrange
        when(loanRepository.findById(1L)).thenReturn(Optional.of(pendingLoan));
        when(loanRepository.save(any(Loan.class))).thenReturn(pendingLoan);

        // Act
        LoanResponse response = loanService.rejectLoan(1L);

        // Assert
        assertThat(response).isNotNull();
        verify(loanRepository, times(1)).save(any(Loan.class));
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void shouldReturnLoansByCustomer() {
        // Arrange
        when(loanRepository.findByCustomerId(1L)).thenReturn(List.of(pendingLoan));

        // Act
        List<LoanResponse> responses = loanService.getLoansByCustomer(1L);

        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getCustomerName()).isEqualTo("Amer Zaatari");
        verify(loanRepository, times(1)).findByCustomerId(1L);
    }
}