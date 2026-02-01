package com.example.libraryweek1.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotsDto {
    private Integer roomId;
    private Integer deskId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
