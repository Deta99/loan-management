package com.loan.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Long id;
    private BigDecimal amount;
    private LocalDateTime paidAt;
    private Long loanId;
}