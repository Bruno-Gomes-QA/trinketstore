package com.reducess.trinketstore.exception;
public class SignUpException extends RuntimeException {
    public SignUpException(String message) {
        super(message);
    }
    public SignUpException(String message, Throwable cause) {
        super(message, cause);
    }
}
