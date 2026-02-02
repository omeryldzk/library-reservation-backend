package com.example.libraryweek1.reservation.repository;

import com.example.libraryweek1.AbstractIntegrationTest;
import com.example.libraryweek1.reservation.entity.ReservationSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class ReservationSlotRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ReservationSlotRepository reservationSlotRepository;

    @BeforeEach
    void setUp() {
        reservationSlotRepository.deleteAll();
    }

    @Test
    void existsByDeskIdAndSlotStart_shouldReturnTrue_whenExists() {
        ReservationSlot slot = new ReservationSlot();
        slot.setDeskId(1);
        slot.setRoomId(1);
        slot.setSlotStart(LocalDateTime.now());
        slot.setSlotEnd(LocalDateTime.now().plusHours(1));
        slot.setBooked(false);
        reservationSlotRepository.save(slot);

        boolean exists = reservationSlotRepository.existsByDeskIdAndSlotStart(1, slot.getSlotStart());
        assertThat(exists).isTrue();
    }

    @Test
    void deleteBySlotEndBefore_shouldDeleteOldSlots() {
        ReservationSlot oldSlot = new ReservationSlot();
        oldSlot.setDeskId(1);
        oldSlot.setRoomId(1);
        oldSlot.setSlotStart(LocalDateTime.now().minusHours(2));
        oldSlot.setSlotEnd(LocalDateTime.now().minusHours(1));
        oldSlot.setBooked(false);
        reservationSlotRepository.save(oldSlot);

        ReservationSlot newSlot = new ReservationSlot();
        newSlot.setDeskId(1);
        newSlot.setRoomId(1);
        newSlot.setSlotStart(LocalDateTime.now());
        newSlot.setSlotEnd(LocalDateTime.now().plusHours(1));
        newSlot.setBooked(false);
        reservationSlotRepository.save(newSlot);

        reservationSlotRepository.deleteBySlotEndBefore(LocalDateTime.now());

        List<ReservationSlot> all = reservationSlotRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getSlotEnd()).isAfter(LocalDateTime.now());
    }

    @Test
    void findSlotsByDeskId_shouldReturnAvailableSlots() {
        ReservationSlot slot = new ReservationSlot();
        slot.setDeskId(1);
        slot.setRoomId(1);
        slot.setSlotStart(LocalDateTime.now());
        slot.setSlotEnd(LocalDateTime.now().plusHours(1));
        slot.setBooked(false);
        reservationSlotRepository.save(slot);

        Optional<List<ReservationSlot>> found = reservationSlotRepository.findSlotsByDeskId(1);
        assertThat(found).isPresent();
        assertThat(found.get()).hasSize(1);
    }

    @Test
    void findFilledSlotsByDeskId_shouldReturnBookedSlots() {
        ReservationSlot slot = new ReservationSlot();
        slot.setDeskId(1);
        slot.setRoomId(1);
        slot.setSlotStart(LocalDateTime.now());
        slot.setSlotEnd(LocalDateTime.now().plusHours(1));
        slot.setBooked(true);
        reservationSlotRepository.save(slot);

        Optional<List<ReservationSlot>> found = reservationSlotRepository.findFilledSlotsByDeskId(1);
        assertThat(found).isPresent();
        assertThat(found.get()).hasSize(1);
    }

    @Test
    void findSlotsByRangeAndDeskId_shouldReturnSlots() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);

        ReservationSlot slot = new ReservationSlot();
        slot.setDeskId(1);
        slot.setRoomId(1);
        slot.setSlotStart(start);
        slot.setSlotEnd(end);
        slot.setBooked(false);
        reservationSlotRepository.save(slot);

        Optional<List<ReservationSlot>> found = reservationSlotRepository.findSlotsByRangeAndDeskId(1, start, end);
        assertThat(found).isPresent();
        assertThat(found.get()).hasSize(1);
    }
}
