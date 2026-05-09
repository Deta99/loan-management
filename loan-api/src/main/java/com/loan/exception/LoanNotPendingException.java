package com.loan.exception;

public class LoanNotPendingException extends RuntimeException {
    public LoanNotPendingException(String message) {
        super(message);
    }
}