package com.example.libraryweek1.reservation.controller;

import com.example.libraryweek1.reservation.dto.ReservationRequest;
import com.example.libraryweek1.reservation.dto.ReservationResponse;
import com.example.libraryweek1.reservation.dto.SlotsDto;
import com.example.libraryweek1.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.ok(reservationService.makeReservation(request));
    }

    @GetMapping("/free-slots/{deskId}")
    public ResponseEntity<List<SlotsDto>> getFreeSlots(@PathVariable Integer deskId) {
        return ResponseEntity.ok(reservationService.getFreeSlots(deskId));
    }

    @GetMapping("/free-slots/{roomId}")
    public ResponseEntity<List<SlotsDto>> getFreeSlotsRoom(@PathVariable Integer roomId) {
        return ResponseEntity.ok(reservationService.getFreeSlotsRoom(roomId));
    }

    @GetMapping("/active/{roomId}")
    public ResponseEntity<List<SlotsDto>> getActiveReservations(@PathVariable Integer roomId) {
        return ResponseEntity.ok(reservationService.getActiveReservations(roomId));
    }

    @PostMapping("/cancel/{reservationId}")
    public ResponseEntity<ReservationResponse> cancelReservation(@PathVariable Long reservationId,
                                                    @RequestBody String reason) {
        // Implement cancellation logic here
        return ResponseEntity.ok(reservationService.cancelReservation(reservationId, reason));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReservationResponse>> getReservationsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(reservationService.getReservationsByUserId(userId));
    }




}