package com.example.libraryweek1.reservation.repository;

import com.example.libraryweek1.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserId(Long userId);

    // Find by reservation ıd
    @Query("SELECT r FROM Reservation r WHERE r.id = :id")
    Optional<Reservation> findByReservationId(@Param("id") Long id);

    // Find by studentId
    @Query("SELECT r FROM Reservation r WHERE r.user.studentId = :studentId ")
    List<Reservation> findByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT r FROM Reservation r WHERE r.user.studentId = :studentId AND r.status = 'CONFIRMED' ")
    Optional<Reservation> findStartedByStudentId(@Param("studentId") Long studentId);

    // Find Confirmed reservations by roomId
    @Query("SELECT r FROM Reservation r WHERE r.roomId = :roomId AND r.status = 'CONFIRMED' ")
    Optional<List<Reservation>> findStartedByRoomId(Integer roomId);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'CONFIRMED'")
    List<Reservation> findActiveReservations(@Param("now") LocalDateTime now);

    @Query("SELECT r FROM Reservation r WHERE r.user.studentId = :studentId AND r.status = 'PENDING' ")
    Optional<Reservation> findPendingByStudentId(@Param("studentId") Long studentId);

    // Find Confirmed reservations by roomId
    @Query("SELECT r FROM Reservation r WHERE r.roomId = :roomId AND r.status = 'PENDING' ")
    Optional<List<Reservation>> findPendingByRoomId(String roomId);



} 