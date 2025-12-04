package com.minibank.customerservice.Exceptions;

public class KycException extends RuntimeException {
    public KycException(String message) {
        super(message);
    }
}
