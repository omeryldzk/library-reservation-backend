package com.example.libraryweek1.reservation.service;

import com.example.libraryweek1.reservation.dto.ReservationRequest;
import com.example.libraryweek1.reservation.dto.ReservationResponse;
import com.example.libraryweek1.reservation.dto.SlotsDto;

import java.util.List;

public interface ReservationService {
    List<SlotsDto> getFreeSlots(Integer deskId);

    List<SlotsDto> getFreeSlotsRoom(Integer roomId);

    List<SlotsDto> getActiveReservations(Integer roomId);

    List<ReservationResponse> getReservationsByUserId(Long userId);

    ReservationResponse cancelReservation(Long reservationId, String reason);

    ReservationResponse makeReservation(ReservationRequest reservationRequest);
}
