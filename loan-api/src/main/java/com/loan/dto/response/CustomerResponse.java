package com.loan.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CustomerResponse {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private LocalDateTime createdAt;
}