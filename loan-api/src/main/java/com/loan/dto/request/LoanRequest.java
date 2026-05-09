package com.loan.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class LoanRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "100.00", message = "Minimum loan amount is 100")
    private BigDecimal amount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.1", message = "Interest rate must be greater than 0")
    private BigDecimal interestRate;

    @NotNull(message = "Term is required")
    @Min(value = 1, message = "Minimum term is 1 month")
    private Integer termMonths;
}