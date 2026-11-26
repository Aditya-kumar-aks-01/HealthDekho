package com.healthdekho.exception;

public class DuplicatePolicyException extends RuntimeException {

    public DuplicatePolicyException(String message) {
        super(message);
    }
}