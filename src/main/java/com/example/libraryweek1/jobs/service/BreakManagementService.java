package com.example.libraryweek1.jobs.service;

import java.time.LocalDateTime;

public interface BreakManagementService {
    long handleCheckIn(Long reservationId, LocalDateTime checkInTime);
    long handleCheckOut(Long reservationId, LocalDateTime checkOutTime);
}
