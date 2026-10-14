package com.userservice.User.exception;

/** Thrown when a request is well-formed but doesn't make sense, e.g. rating yourself. Maps to HTTP 400. */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
