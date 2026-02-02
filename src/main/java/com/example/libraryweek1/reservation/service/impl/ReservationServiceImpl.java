package com.example.libraryweek1.reservation.service.impl;

import com.example.libraryweek1.jobs.service.JobService;
import com.example.libraryweek1.reservation.dto.ReservationRequest;
import com.example.libraryweek1.reservation.dto.ReservationResponse;
import com.example.libraryweek1.reservation.dto.SlotsDto;
import com.example.libraryweek1.reservation.entity.Reservation;
import com.example.libraryweek1.reservation.entity.ReservationSlot;
import com.example.libraryweek1.reservation.entity.ReservationStatus;
import com.example.libraryweek1.reservation.exception.NotConsecutiveSlotsException;
import com.example.libraryweek1.reservation.exception.ResourceNotFoundException;
import com.example.libraryweek1.mapper.DataMapper;
import com.example.libraryweek1.reservation.repository.ReservationRepository;
import com.example.libraryweek1.reservation.repository.ReservationSlotRepository;
import com.example.libraryweek1.reservation.service.ReservationService;
import com.example.libraryweek1.user.entity.User;
import com.example.libraryweek1.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final DataMapper dataMapper;
    private final UserRepository userRepository;
    private final JobScheduler jobScheduler;
    private final JobService jobService;

    @Override
    public List<SlotsDto> getFreeSlots(Integer deskId){
        List<ReservationSlot> freeSlots = reservationSlotRepository.findSlotsByDeskId(deskId)
                .orElseThrow(() -> new ResourceNotFoundException("No Available Slots"));

        return freeSlots.stream()
                .map((slot) -> dataMapper.toSlotsDto(slot))
                .toList(); // Converts the stream back to a List
    }


    @Override
    public List<SlotsDto> getFreeSlotsRoom(Integer roomId) {
        List<ReservationSlot> freeSlots = reservationSlotRepository.findSlotsByRoomId(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("No Available Slots"));

        return freeSlots.stream()
                .map((slot) -> dataMapper.toSlotsDto(slot))
                .toList(); // Converts the stream back to a List
    }

    @Override
    public List<SlotsDto> getActiveReservations(Integer roomId) {
        List<Reservation> activeSlots = reservationRepository.findStartedByRoomId(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("No Active Reservations"));

        return activeSlots.stream()
                .map((reservation) -> dataMapper.toSlotsDto(reservation))
                .toList(); // Converts the stream back to a List
    }

    @Override
    @Transactional
    public ReservationResponse cancelReservation(Long reservationId, String reason) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation Not Found"));

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancellationReason(reason);
        Reservation updatedReservation = reservationRepository.save(reservation);

        return dataMapper.toReservationResponse(updatedReservation);
    }

    @Override
    @Transactional
    public void completeReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation Not Found"));

        reservation.setStatus(ReservationStatus.COMPLETED);
        Reservation updatedReservation = reservationRepository.save(reservation);

        dataMapper.toReservationResponse(updatedReservation);
    }

    @Override
    public List<ReservationResponse> getReservationsByUserId(Long userId) {
        List<Reservation> reservations = reservationRepository.findByUserId(userId);
        return reservations.stream()
                .map(dataMapper::toReservationResponse)
                .toList();
    }


    @Override
    @Transactional
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
        Reservation reservation = dataMapper.toReservation(freeSlots, reservationRequest, user);
        Reservation savedReservation = reservationRepository.save(reservation);
        freeSlots.forEach(slot -> {
            slot.setBooked(true);
            slot.setReservation(savedReservation);
        });
        reservationSlotRepository.saveAll(freeSlots);
        jobScheduler.schedule(
                reservationRequest.getStartTime(),
                () -> jobService.checkUserCheckIn(savedReservation.getId(), user.getStudentId().toString())
        );
        jobScheduler.schedule(
                reservationRequest.getEndTime(),
                () -> jobService.checkUserCompleted(savedReservation.getId())
        );
        return dataMapper.toReservationResponse(savedReservation);
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
