package com.drivingsimulator.exception;

/** Raised when input would create an invalid simulation. */
public final class ValidationException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}
