package com.example.libraryweek1.reservation.repository;

import com.example.libraryweek1.AbstractIntegrationTest;
import com.example.libraryweek1.reservation.entity.Reservation;
import com.example.libraryweek1.reservation.entity.ReservationStatus;
import com.example.libraryweek1.user.entity.User;
import com.example.libraryweek1.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class ReservationRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Reservation testReservation;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .studentId(12345L)
                .email("test@example.com")
                .passwordHash("hashed")
                .enabled(true)
                .build();
        userRepository.save(testUser);

        testReservation = Reservation.builder()
                .user(testUser)
                .roomId(1)
                .deskId(11)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .status(ReservationStatus.CONFIRMED)
                .onBreak(false)
                .breakTime(0L)
                .build();
        reservationRepository.save(testReservation);
    }

    @Test
    void findByUserId_shouldReturnReservations() {
        List<Reservation> found = reservationRepository.findByUserId(testUser.getId());
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void findByReservationId_shouldReturnReservation() {
        Optional<Reservation> found = reservationRepository.findByReservationId(testReservation.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(testReservation.getId());
    }

    @Test
    void findByStudentId_shouldReturnReservations() {
        List<Reservation> found = reservationRepository.findByStudentId(testUser.getStudentId());
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getUser().getStudentId()).isEqualTo(testUser.getStudentId());
    }

    @Test
    void findStartedByStudentId_shouldReturnConfirmedReservation() {
        Optional<Reservation> found = reservationRepository.findStartedByStudentId(testUser.getStudentId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void findStartedByRoomId_shouldReturnConfirmedReservations() {
        Optional<List<Reservation>> found = reservationRepository.findStartedByRoomId(1);
        assertThat(found).isPresent();
        assertThat(found.get()).hasSize(1);
    }

    @Test
    void findPendingByStudentId_shouldReturnPendingReservation() {
        Reservation pendingReservation = Reservation.builder()
                .user(testUser)
                .roomId(2)
                .deskId(22)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(3))
                .status(ReservationStatus.PENDING)
                .onBreak(false)
                .breakTime(0L)
                .build();
        reservationRepository.save(pendingReservation);

        Optional<Reservation> found = reservationRepository.findPendingByStudentId(testUser.getStudentId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    void findPendingByRoomId_shouldReturnPendingReservations() {
        Reservation pendingReservation = Reservation.builder()
                .user(testUser)
                .roomId(1)
                .deskId(12)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(3))
                .status(ReservationStatus.PENDING)
                .onBreak(false)
                .breakTime(0L)
                .build();
        reservationRepository.save(pendingReservation);

        Optional<List<Reservation>> found = reservationRepository.findPendingByRoomId(1);
        assertThat(found).isPresent();
        assertThat(found.get()).hasSize(1);
    }

    @Test
    void findActiveReservations_shouldReturnConfirmedReservations() {
        List<Reservation> found = reservationRepository.findActiveReservations(LocalDateTime.now());
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }
}
