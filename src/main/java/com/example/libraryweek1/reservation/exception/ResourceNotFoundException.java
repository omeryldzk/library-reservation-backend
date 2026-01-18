package com.example.libraryweek1.reservation.exception;

import com.example.libraryweek1.config.exception.ApiBaseException;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiBaseException {
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
} 