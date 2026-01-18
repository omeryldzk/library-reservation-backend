package com.example.libraryweek1.reservation.service.impl;

import com.example.libraryweek1.reservation.dto.ReservationRequest;
import com.example.libraryweek1.reservation.dto.ReservationResponse;
import com.example.libraryweek1.reservation.dto.SlotsDto;
import com.example.libraryweek1.reservation.entity.Reservation;
import com.example.libraryweek1.reservation.entity.ReservationSlot;
import com.example.libraryweek1.reservation.exception.NotConsecutiveSlotsException;
import com.example.libraryweek1.reservation.exception.ResourceNotFoundException;
import com.example.libraryweek1.reservation.mapper.ReservationSlotMapper;
import com.example.libraryweek1.reservation.repository.ReservationRepository;
import com.example.libraryweek1.reservation.repository.ReservationSlotRepository;
import com.example.libraryweek1.reservation.service.ReservationService;
import com.example.libraryweek1.user.entity.User;
import com.example.libraryweek1.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final ReservationSlotMapper reservationSlotMapper;
    private final UserRepository userRepository;

    @Override
    public List<SlotsDto> getFreeSlots(Integer deskId){
        List<ReservationSlot> freeSlots = reservationSlotRepository.findSlotsByDeskId(deskId)
                .orElseThrow(() -> new ResourceNotFoundException("No Available Slots"));

        return freeSlots.stream()
                .map((slot) -> reservationSlotMapper.toSlotsDto(slot))
                .toList(); // Converts the stream back to a List
    }

    @Override
    public ReservationResponse makeReservation(ReservationRequest reservationRequest){
        User user = userRepository.findById(reservationRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        List<ReservationSlot> freeSlots = reservationSlotRepository.findPessimisticSlotsByRangeAndDeskId(
                reservationRequest.getDeskId(), reservationRequest.getStartTime(), reservationRequest.getEndTime()
                )
                .orElseThrow(() -> new ResourceNotFoundException("No Available Slots"));
        if(!validateReservation(freeSlots)){
            throw new NotConsecutiveSlotsException("Reservation Slots Are Not Consecutive");
        }
        Reservation reservation = reservationSlotMapper.toReservation(freeSlots, reservationRequest, user);
        Reservation savedReservation = reservationRepository.save(reservation);
        return reservationSlotMapper.toReservationResponse(savedReservation);
    }
    public boolean validateReservation(List<ReservationSlot> freeSlots) {
        // Sort the slots by slotStart
        freeSlots.sort(Comparator.comparing(ReservationSlot::getSlotStart));

        // Check if each slot is consecutive
        for (int i = 0; i < freeSlots.size() - 1; i++) {
            if (!freeSlots.get(i).getSlotEnd().equals(freeSlots.get(i + 1).getSlotStart())) {
                return false; // Not consecutive
            }
        }

        return true; // All slots are consecutive
    }


}
