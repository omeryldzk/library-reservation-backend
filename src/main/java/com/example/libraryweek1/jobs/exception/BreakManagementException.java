package com.example.libraryweek1.jobs.exception;

import com.example.libraryweek1.config.ApiBaseException;
import org.springframework.http.HttpStatus;

public class BreakManagementException extends ApiBaseException {
    public BreakManagementException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
