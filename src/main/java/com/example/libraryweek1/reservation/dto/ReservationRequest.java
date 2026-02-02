package com.example.libraryweek1.reservation.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Room ID is required")
    @Positive(message = "Room ID must be positive")
    private Integer roomId;

    @NotNull(message = "Desk ID is required")
    @Positive(message = "Desk ID must be positive")
    private Integer deskId;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;
} 