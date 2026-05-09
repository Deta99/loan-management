package com.loan.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanApprovedEvent {
    private Long loanId;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private BigDecimal amount;
    private Integer termMonths;
}