package com.example.libraryweek1.mapper;

import com.example.libraryweek1.reservation.dto.ReservationRequest;
import com.example.libraryweek1.reservation.dto.ReservationResponse;
import com.example.libraryweek1.reservation.dto.SlotsDto;
import com.example.libraryweek1.reservation.entity.Reservation;
import com.example.libraryweek1.reservation.entity.ReservationSlot;
import com.example.libraryweek1.reservation.entity.ReservationStatus;
import com.example.libraryweek1.user.entity.User;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
public class DataMapper {

    private final ObjectMapper objectMapper;

    // Spring will automatically inject the configured ObjectMapper here
    public DataMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    public SlotsDto toSlotsDto(ReservationSlot slot) {
        return SlotsDto.builder()
                .roomId(slot.getRoomId())
                .deskId(slot.getDeskId())
                .startTime(slot.getSlotStart())
                .endTime(slot.getSlotEnd())
                .build();
    }

    public SlotsDto toSlotsDto(Reservation reservation) {
        return SlotsDto.builder()
                .roomId(reservation.getRoomId())
                .deskId(reservation.getDeskId())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .build();
    }

    public Reservation toReservation(List<ReservationSlot> freeSlots,
                                     ReservationRequest reservationRequest,User user) {
        Reservation reservation =  Reservation.builder()
                .roomId(reservationRequest.getRoomId())
                .deskId(reservationRequest.getDeskId())
                .user(user)
                .startTime(reservationRequest.getStartTime())
                .endTime(reservationRequest.getEndTime())
                .status(ReservationStatus.PENDING)
                .build();
        freeSlots.forEach(slot -> reservation.addSlot(slot));
        return reservation;
    }

    public ReservationResponse toReservationResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .userId(reservation.getUser().getId())
                .roomId(reservation.getRoomId())
                .deskId(reservation.getDeskId())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .status(reservation.getStatus())
                .cancellationReason(reservation.getCancellationReason())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }


}