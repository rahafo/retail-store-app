package com.retail.discount.retaildiscountapplication.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private int status;
    private LocalDateTime timestamp;
    private List<String> errors;

    public ErrorResponse(String message, int status, LocalDateTime timestamp) {
        this(message, status, timestamp, null);
    }
}