package com.example.libraryweek1.jobs.service.impl;

import com.example.libraryweek1.jobs.service.JobService;
import com.example.libraryweek1.reservation.entity.Reservation;
import com.example.libraryweek1.reservation.entity.ReservationStatus;
import com.example.libraryweek1.reservation.exception.ResourceNotFoundException;
import com.example.libraryweek1.reservation.repository.ReservationRepository;
import com.example.libraryweek1.reservation.service.ReservationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class JobServiceImpl implements JobService {

    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private StringRedisTemplate redisTemplate;


    public JobServiceImpl(ReservationRepository reservationRepository, ReservationService reservationService,
    @Qualifier("activeUsersRedisTemplate") StringRedisTemplate redisTemplate) {
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
    }

    @Override
    public void checkUserCheckIn(Long reservationId, String studentId) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);

        if(reservationOpt.isPresent()) {
            Reservation reservation = reservationOpt.get();
            if(reservation.getStatus() == ReservationStatus.CONFIRMED) {
                // User has checked in, no action needed
                return;
            } else {
                // User has not checked in, cancel the reservation
                // Check in Redis if the user has checked in as final verification
                boolean checkStudent = redisTemplate.opsForSet().isMember("library:users:inside", studentId);
                if(checkStudent == Boolean.FALSE) {
                    reservationService.cancelReservation(reservationId, "User did not check in on time");
                }
            }
        }
        else {
            // Reservation not found, handle accordingly
            throw new ResourceNotFoundException("Reservation not found for ID: " + reservationId);
        }

    }

    @Override
    public void checkUserCompleted(Long reservationId) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);

        if(reservationOpt.isPresent()) {
            Reservation reservation = reservationOpt.get();
            if(reservation.getStatus() == ReservationStatus.CANCELLED) {
                // Reservation cancelled, no action needed
                return;
            } else {
                // User has not checked in, cancel the reservation
                reservationService.completeReservation(reservationId);
            }
        }
        else {
            // Reservation not found, handle accordingly
            throw new ResourceNotFoundException("Reservation not found for ID: " + reservationId);
        }
    }

}
