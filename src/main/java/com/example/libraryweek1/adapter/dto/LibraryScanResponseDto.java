package com.example.libraryweek1.adapter.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LibraryScanResponseDto {
    private long remainingBreakMinutes;
    private String message;

    // Ensure the constructor is public
    public LibraryScanResponseDto(long remainingBreakMinutes, String message) {
        this.remainingBreakMinutes = remainingBreakMinutes;
        this.message = message;
    }
} 