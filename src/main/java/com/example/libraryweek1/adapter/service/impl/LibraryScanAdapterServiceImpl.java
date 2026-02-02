package com.example.libraryweek1.adapter.service.impl;

import com.example.libraryweek1.adapter.dto.LibraryScanDto;
import com.example.libraryweek1.adapter.dto.LibraryScanResponseDto;
import com.example.libraryweek1.adapter.service.LibraryScanAdapterService;
import com.example.libraryweek1.jobs.service.BreakManagementService;
import com.example.libraryweek1.reservation.entity.Reservation;
import com.example.libraryweek1.reservation.repository.ReservationRepository;
import com.example.libraryweek1.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class LibraryScanAdapterServiceImpl implements LibraryScanAdapterService {

    private final ReservationRepository reservationRepository;
    private BreakManagementService breakManagementService;
    private final ReservationService reservationService;
    private StringRedisTemplate redisTemplate;

    public LibraryScanAdapterServiceImpl(ReservationRepository reservationRepository,
                                     ReservationService reservationService,
                                     @Qualifier("activeUsersRedisTemplate") StringRedisTemplate redisTemplate) {
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Async
    public CompletableFuture<LibraryScanResponseDto> processLibraryScan(LibraryScanDto scanDto) {
        Optional<Reservation> reservationOpt = reservationRepository.findStartedByStudentId(scanDto.getStudentId());
        log.info("Processing scan for student ID: {} ", scanDto.getStudentId());

        switch (scanDto.getScanType()) {
            case ENTER:
                log.info("Student {} is checking in.", scanDto.getStudentId());
                // Implement check-in logic here
                return handleEnterScan(scanDto, reservationOpt);

            case EXIT:
                log.info("Student {} is checking out.", scanDto.getStudentId());
                return handleExitScan(scanDto, reservationOpt);

            default:
                log.error("Unknown scan type for student ID: {}", scanDto.getStudentId());
                return CompletableFuture.completedFuture(
                        new LibraryScanResponseDto(0, "Unknown scan type"));
        }
    }

    private CompletableFuture<LibraryScanResponseDto> handleEnterScan(LibraryScanDto scanDto, Optional<Reservation> reservation) {
        log.info("Starting async enter scan for student: {}", scanDto.getStudentId());

        return CompletableFuture.supplyAsync(() -> {
                // Even if no reservation, we log the scan
                // Keep the users inside set updated
                    redisTemplate.opsForSet().add("library:users:inside", scanDto.getStudentId().toString());
                    return scanDto;
                })
                .thenApply(dto -> {
                    // If there's a reservation, handle check-in
                    if (reservation.isPresent()) {
                        long remainingBreakMinutes = breakManagementService.handleCheckIn(reservation.get().getId(), scanDto.getTimestamp());
                        return new LibraryScanResponseDto(remainingBreakMinutes, "Checked in");
                    }
                    // No reservation found
                    return new LibraryScanResponseDto(0L, "No reservation");
                })
                .exceptionally(ex -> new LibraryScanResponseDto(0, "Error: " + ex.getMessage()));
    }

    private CompletableFuture<LibraryScanResponseDto> handleExitScan(LibraryScanDto scanDto, Optional<Reservation> reservation) {
        log.info("Starting async enter scan for student: {}", scanDto.getStudentId());

        return CompletableFuture.supplyAsync(() -> {
                    // Even if no reservation, we log the scan
                    // Keep the users inside set updated
                    redisTemplate.opsForSet().remove("library:users:inside", scanDto.getStudentId().toString());
                    return scanDto;
                })
                .thenApply(dto -> {
                    // If there's a reservation, handle check-in
                    if (reservation.isPresent()) {
                        long remainingBreakMinutes = breakManagementService.handleCheckOut(reservation.get().getId(), scanDto.getTimestamp());
                        return new LibraryScanResponseDto(remainingBreakMinutes, "Checked in");
                    }
                    // No reservation found
                    return new LibraryScanResponseDto(0L, "No reservation");
                })
                .exceptionally(ex -> new LibraryScanResponseDto(0, "Error: " + ex.getMessage()));
    }



    }


