package com.loan.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LoanResponse {
    private Long id;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private Integer termMonths;
    private String status;
    private LocalDateTime createdAt;
    private String customerName;
}