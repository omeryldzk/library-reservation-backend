package com.example.libraryweek1.jobs.service.impl;


import com.example.libraryweek1.jobs.dto.BreakMonitorInfo;
import com.example.libraryweek1.jobs.exception.BreakManagementException;
import com.example.libraryweek1.jobs.service.BreakManagementService;
import com.example.libraryweek1.reservation.entity.Reservation;
import com.example.libraryweek1.reservation.entity.ReservationStatus;
import com.example.libraryweek1.reservation.exception.ResourceNotFoundException;
import com.example.libraryweek1.reservation.repository.ReservationRepository;
import com.example.libraryweek1.reservation.service.ReservationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

@Service
public class BreakManagementServiceImpl implements BreakManagementService {
    private final TransactionTemplate transactionTemplate;

    private final ReservationService reservationService;

    private final RedisTemplate<String, Object> redisTemplate;

    private final ReservationRepository reservationRepository;

    private static final String BREAK_MONITOR_KEY_PREFIX = "break_monitor:";
    private static final long MAX_BREAK_DURATION_MINUTES = 30; // Example maximum break duration

    public BreakManagementServiceImpl(TransactionTemplate transactionTemplate,
                                      ReservationService reservationService,
    @Qualifier("breakMonitorRedisTemplate") RedisTemplate<String, Object> redisTemplate,
                                      ReservationRepository reservationRepository) {
        this.transactionTemplate = transactionTemplate;
        this.reservationService = reservationService;
        this.redisTemplate = redisTemplate;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public long handleCheckIn(Long reservationId, LocalDateTime checkInTime) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation Not Found"));
        // User is returning from break
        if (reservation.getBreakTime() != null) {
            Long breakStartTime = reservation.getBreakTime();
            BreakMonitorInfo monitorInfo = (BreakMonitorInfo) redisTemplate.opsForValue()
                    .get(BREAK_MONITOR_KEY_PREFIX + reservationId);

            // Break time exceeded maximum allowed duration
            // Key ttl expired so cancel the reservation
            if (monitorInfo == null) {
                transactionTemplate.execute(status -> {
                    reservationService.cancelReservation(reservationId, "Break time exceeded maximum allowed duration");
                    return null;
                });
            }

            // Break is within allowed duration, calculate remaining break time
            long breakDurationMinutes = java.time.Duration.between(
                    LocalDateTime.ofEpochSecond(breakStartTime, 0, java.time.ZoneOffset.UTC),
                    checkInTime).toMinutes();
            long remainingBreakMinutes = MAX_BREAK_DURATION_MINUTES - breakDurationMinutes;
            transactionTemplate.execute(status -> {
                // Clear break time and update reservation status
                reservation.setBreakTime(remainingBreakMinutes);
                reservationRepository.save(reservation);
                // Remove break monitor info from Redis
                redisTemplate.delete(BREAK_MONITOR_KEY_PREFIX + reservationId);
                return null;
            });
            return Math.max(remainingBreakMinutes, 0);
        }
        // User is checking in for the first time
        else {
            transactionTemplate.execute(status -> {
                // Clear break time and update reservation status
                reservation.setStatus(ReservationStatus.CONFIRMED);
                reservation.setBreakTime(MAX_BREAK_DURATION_MINUTES);
                reservationRepository.save(reservation);
                return null;
            });
            return MAX_BREAK_DURATION_MINUTES;
        }
    }

    @Override
    public long handleCheckOut(Long reservationId, LocalDateTime checkOutTime) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation Not Found"));

        try{
            redisTemplate.opsForValue().set(BREAK_MONITOR_KEY_PREFIX + reservationId,
                    new BreakMonitorInfo(reservationId, checkOutTime),
                    java.time.Duration.ofMinutes(reservation.getBreakTime()));
            return reservation.getBreakTime();
        }
        catch (Exception e){
            throw new BreakManagementException("Failed to start break monitoring for reservation ID: " + reservationId);
        }
    }




}
