package com.example.libraryweek1.reservation.repository;

import com.example.libraryweek1.reservation.entity.ReservationSlot;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationSlotRepository extends JpaRepository<ReservationSlot, Long> {

    // Check if a slot exists to avoid UniqueConstraint violations during generation
    boolean existsByDeskIdAndSlotStart(Integer deskId, LocalDateTime slotStart);

    // Efficiently delete old slots
    @Modifying
    @Query("DELETE FROM ReservationSlot s WHERE s.slotEnd < :cutoffTime")
    void deleteBySlotEndBefore(LocalDateTime cutoffTime);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    @Query("SELECT r FROM ReservationSlot r WHERE r.deskId = :DeskId AND r.isBooked = false")
    Optional<List<ReservationSlot>> findSlotsPessimisticByDeskId(@Param("deskId") Integer DeskId);

    @Query("SELECT r FROM ReservationSlot r WHERE r.deskId = :DeskId AND r.isBooked = false")
    Optional<List<ReservationSlot>> findSlotsByDeskId(@Param("deskId") Integer DeskId);

    @Query("SELECT r FROM ReservationSlot r WHERE r.roomId = :roomId AND r.isBooked = false")
    Optional<List<ReservationSlot>> findSlotsByRoomId(@Param("roomId") Integer DeskId);



    @Query("SELECT r FROM ReservationSlot r WHERE r.deskId = :DeskId AND r.isBooked = true")
    Optional<List<ReservationSlot>> findFilledSlotsByDeskId(@Param("deskId") Integer DeskId);

    @Query("SELECT r FROM ReservationSlot r WHERE r.isBooked = false")
    Optional<List<ReservationSlot>> findAllSlots();

    @Query("SELECT r FROM ReservationSlot r WHERE r.deskId = :DeskId AND (r.slotStart <= :StartTime AND " +
            "r.slotEnd >= :EndTime)")
    Optional<List<ReservationSlot>> findSlotsByRangeAndDeskId(@Param("deskId") Integer DeskId,@Param("slotStart") LocalDateTime slotStart,@Param("deskId") LocalDateTime slotEnd);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    @Query("SELECT r FROM ReservationSlot r WHERE r.deskId = :DeskId AND (r.slotStart >= :StartTime OR " +
            "r.slotEnd <= :EndTime)")
    Optional<List<ReservationSlot>> findPessimisticSlotsByRangeAndDeskId(@Param("deskId") Integer DeskId,@Param("slotStart") LocalDateTime slotStart,@Param("deskId") LocalDateTime slotEnd);




}