package com.example.libraryweek1.reservation.service.impl;

import com.example.libraryweek1.mapper.DataMapper;
import com.example.libraryweek1.reservation.dto.ReservationRequest;
import com.example.libraryweek1.reservation.dto.ReservationResponse;
import com.example.libraryweek1.reservation.dto.SlotsDto;
import com.example.libraryweek1.reservation.entity.Reservation;
import com.example.libraryweek1.reservation.entity.ReservationSlot;
import com.example.libraryweek1.reservation.entity.ReservationStatus;
import com.example.libraryweek1.reservation.exception.NotConsecutiveSlotsException;
import com.example.libraryweek1.reservation.exception.ResourceNotFoundException;
import com.example.libraryweek1.reservation.repository.ReservationRepository;
import com.example.libraryweek1.reservation.repository.ReservationSlotRepository;
import com.example.libraryweek1.user.entity.User;
import com.example.libraryweek1.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationSlotRepository reservationSlotRepository;
    @Mock
    private DataMapper dataMapper;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @Test
    void getFreeSlots_shouldReturnSlots_whenFound() {
        Integer deskId = 1;
        ReservationSlot slot = new ReservationSlot();
        SlotsDto slotsDto = new SlotsDto();

        when(reservationSlotRepository.findSlotsByDeskId(deskId)).thenReturn(Optional.of(List.of(slot)));
        when(dataMapper.toSlotsDto(slot)).thenReturn(slotsDto);

        List<SlotsDto> result = reservationService.getFreeSlots(deskId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(slotsDto);
    }

    @Test
    void getFreeSlots_shouldThrowException_whenNoSlotsFound() {
        Integer deskId = 1;
        when(reservationSlotRepository.findSlotsByDeskId(deskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.getFreeSlots(deskId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No Available Slots");
    }

    @Test
    void getFreeSlotsRoom_shouldReturnSlots_whenFound() {
        Integer roomId = 1;
        ReservationSlot slot = new ReservationSlot();
        SlotsDto slotsDto = new SlotsDto();

        when(reservationSlotRepository.findSlotsByRoomId(roomId)).thenReturn(Optional.of(List.of(slot)));
        when(dataMapper.toSlotsDto(slot)).thenReturn(slotsDto);

        List<SlotsDto> result = reservationService.getFreeSlotsRoom(roomId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(slotsDto);
    }

    @Test
    void getFreeSlotsRoom_shouldThrowException_whenNoSlotsFound() {
        Integer roomId = 1;
        when(reservationSlotRepository.findSlotsByRoomId(roomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.getFreeSlotsRoom(roomId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No Available Slots");
    }

    @Test
    void getActiveReservations_shouldReturnReservations_whenFound() {
        Integer roomId = 1;
        Reservation reservation = new Reservation();
        SlotsDto slotsDto = new SlotsDto();

        when(reservationRepository.findStartedByRoomId(roomId)).thenReturn(Optional.of(List.of(reservation)));
        when(dataMapper.toSlotsDto(reservation)).thenReturn(slotsDto);

        List<SlotsDto> result = reservationService.getActiveReservations(roomId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(slotsDto);
    }

    @Test
    void getActiveReservations_shouldThrowException_whenNoReservationsFound() {
        Integer roomId = 1;
        when(reservationRepository.findStartedByRoomId(roomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.getActiveReservations(roomId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No Active Reservations");
    }

    @Test
    void cancelReservation_shouldCancelReservation_whenFound() {
        Long reservationId = 1L;
        String reason = "Changed plans";
        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        Reservation updatedReservation = new Reservation();
        updatedReservation.setId(reservationId);
        updatedReservation.setStatus(ReservationStatus.CANCELLED);
        updatedReservation.setCancellationReason(reason);
        ReservationResponse response = new ReservationResponse();
        response.setStatus(ReservationStatus.CANCELLED);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(reservation)).thenReturn(updatedReservation);
        when(dataMapper.toReservationResponse(updatedReservation)).thenReturn(response);

        ReservationResponse result = reservationService.cancelReservation(reservationId, reason);

        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        verify(reservationRepository).save(reservation);
    }

    @Test
    void cancelReservation_shouldThrowException_whenReservationNotFound() {
        Long reservationId = 1L;
        String reason = "Changed plans";
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.cancelReservation(reservationId, reason))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Reservation Not Found");
    }

    @Test
    void getReservationsByUserId_shouldReturnReservations_whenFound() {
        Long userId = 1L;
        Reservation reservation = new Reservation();
        ReservationResponse response = new ReservationResponse();

        when(reservationRepository.findByUserId(userId)).thenReturn(List.of(reservation));
        when(dataMapper.toReservationResponse(reservation)).thenReturn(response);

        List<ReservationResponse> result = reservationService.getReservationsByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(response);
    }

    @Test
    void makeReservation_shouldCreateReservation_whenValidRequest() {
        ReservationRequest request = new ReservationRequest();
        request.setUserId(1L);
        request.setDeskId(1);
        request.setStartTime(LocalDateTime.now());
        request.setEndTime(LocalDateTime.now().plusHours(2));

        User user = new User();

        ReservationSlot slot1 = new ReservationSlot();
        slot1.setSlotStart(LocalDateTime.of(2024, 1, 1, 10, 0));
        slot1.setSlotEnd(LocalDateTime.of(2024, 1, 1, 11, 0));

        ReservationSlot slot2 = new ReservationSlot();
        slot2.setSlotStart(LocalDateTime.of(2024, 1, 1, 11, 0));
        slot2.setSlotEnd(LocalDateTime.of(2024, 1, 1, 12, 0));

        // Use ArrayList to allow sorting/modification if needed by the service
        List<ReservationSlot> slots = new ArrayList<>(Arrays.asList(slot1, slot2));

        Reservation reservation = new Reservation();
        Reservation savedReservation = new Reservation();
        ReservationResponse response = new ReservationResponse();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reservationSlotRepository.findPessimisticSlotsByRangeAndDeskId(
                request.getDeskId(), request.getStartTime(), request.getEndTime()))
                .thenReturn(Optional.of(slots));
        when(dataMapper.toReservation(slots, request, user)).thenReturn(reservation);
        when(reservationRepository.save(reservation)).thenReturn(savedReservation);
        when(dataMapper.toReservationResponse(savedReservation)).thenReturn(response);

        ReservationResponse result = reservationService.makeReservation(request);

        assertThat(result).isEqualTo(response);
        verify(reservationRepository).save(reservation);
    }

    @Test
    void makeReservation_shouldThrowException_whenUserNotFound() {
        ReservationRequest request = new ReservationRequest();
        request.setUserId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.makeReservation(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User Not Found");
    }

    @Test
    void makeReservation_shouldThrowException_whenSlotsNotFound() {
        ReservationRequest request = new ReservationRequest();
        request.setUserId(1L);
        request.setDeskId(1);
        request.setStartTime(LocalDateTime.now());
        request.setEndTime(LocalDateTime.now().plusHours(1));

        User user = new User();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reservationSlotRepository.findPessimisticSlotsByRangeAndDeskId(any(), any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.makeReservation(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No Available Slots");
    }

    @Test
    void makeReservation_shouldThrowException_whenSlotsNotConsecutive() {
        ReservationRequest request = new ReservationRequest();
        request.setUserId(1L);
        request.setDeskId(1);
        request.setStartTime(LocalDateTime.now());
        request.setEndTime(LocalDateTime.now().plusHours(2));

        User user = new User();

        ReservationSlot slot1 = new ReservationSlot();
        slot1.setSlotStart(LocalDateTime.of(2024, 1, 1, 10, 0));
        slot1.setSlotEnd(LocalDateTime.of(2024, 1, 1, 11, 0));

        ReservationSlot slot2 = new ReservationSlot();
        slot2.setSlotStart(LocalDateTime.of(2024, 1, 1, 12, 0)); // Gap here
        slot2.setSlotEnd(LocalDateTime.of(2024, 1, 1, 13, 0));

        List<ReservationSlot> slots = new ArrayList<>(Arrays.asList(slot1, slot2));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reservationSlotRepository.findPessimisticSlotsByRangeAndDeskId(any(), any(), any()))
                .thenReturn(Optional.of(slots));

        assertThatThrownBy(() -> reservationService.makeReservation(request))
                .isInstanceOf(NotConsecutiveSlotsException.class)
                .hasMessage("Reservation Slots Are Not Consecutive");
    }
}
