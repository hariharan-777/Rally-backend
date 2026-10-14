package com.userservice.User.exception;

/** Thrown when something has already been recorded, e.g. the same rating or match result twice. Maps to HTTP 409. */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
