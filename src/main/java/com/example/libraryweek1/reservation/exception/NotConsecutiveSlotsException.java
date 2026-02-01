package com.example.libraryweek1.reservation.exception;

import com.example.libraryweek1.config.ApiBaseException;
import org.springframework.http.HttpStatus;

public class NotConsecutiveSlotsException extends ApiBaseException {
    public NotConsecutiveSlotsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

}
