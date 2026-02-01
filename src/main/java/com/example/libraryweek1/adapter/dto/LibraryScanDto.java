package com.example.libraryweek1.adapter.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LibraryScanDto {
    @NotNull(message = "Student ID is required")
    private Long studentId;
    
    @NotNull(message = "Scan type is required")
    private ScanType scanType;
    
    @NotNull(message = "Timestamp is required")
    private LocalDateTime timestamp;
} 