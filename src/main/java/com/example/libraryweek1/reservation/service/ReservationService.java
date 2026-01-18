package com.example.libraryweek1.reservation.service;

import com.example.libraryweek1.reservation.dto.ReservationRequest;
import com.example.libraryweek1.reservation.dto.ReservationResponse;
import com.example.libraryweek1.reservation.dto.SlotsDto;

import java.util.List;

public interface ReservationService {
    List<SlotsDto> getFreeSlots(Integer deskId);

    ReservationResponse makeReservation(ReservationRequest reservationRequest);
}
