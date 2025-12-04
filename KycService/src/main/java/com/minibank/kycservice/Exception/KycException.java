package com.minibank.kycservice.Exception;

public class KycException extends RuntimeException {
    public KycException(String message) {
        super(message);
    }
}