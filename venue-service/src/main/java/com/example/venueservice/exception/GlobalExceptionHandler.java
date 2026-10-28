package com.example.venueservice.exception;

import com.example.venueservice.service.VenueService.InvalidSearchException;
import com.example.venueservice.service.VenueService.VenueNotFoundException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(VenueNotFoundException.class)
    ResponseEntity<Map<String, String>> venueNotFound() {
        return error(HttpStatus.NOT_FOUND, "venue not found");
    }

    @ExceptionHandler(InvalidSearchException.class)
    ResponseEntity<Map<String, String>> invalidSearch(InvalidSearchException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    // BindException covers both @RequestBody failures (MethodArgumentNotValidException is a subclass)
    // and the search query parameters bound with @ModelAttribute.
    @ExceptionHandler(BindException.class)
    ResponseEntity<Map<String, String>> invalidRequest(BindException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.isBindingFailure()
                        ? "invalid value for " + error.getField()
                        : error.getField() + " " + error.getDefaultMessage())
                .orElse("invalid request");
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<Map<String, String>> typeMismatch(MethodArgumentTypeMismatchException exception) {
        return error(HttpStatus.BAD_REQUEST, "invalid value for " + exception.getName());
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }
}
