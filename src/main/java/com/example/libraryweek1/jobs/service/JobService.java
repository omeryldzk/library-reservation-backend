package com.example.libraryweek1.jobs.service;

public interface JobService {
    void checkUserCheckIn(Long reservationId, String studentId);
    void checkUserCompleted(Long reservationId);
}
