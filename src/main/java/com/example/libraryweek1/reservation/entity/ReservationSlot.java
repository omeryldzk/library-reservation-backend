package com.example.libraryweek1.reservation.entity;

import com.example.libraryweek1.reservation.dto.SlotsDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reservation_slots",
        uniqueConstraints = {
                // THIS IS THE KEY: The database will refuse to save a row
                // if a slot for this Desk at this Time already exists.
                @UniqueConstraint(columnNames = {"deskId", "slotStart"})
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer deskId; // Logic: A slot belongs to a specific desk

    @Column(nullable = false)
    private Integer roomId; // Logic: A slot belongs to a specific desk


    @Column(nullable = false)
    private LocalDateTime slotStart; // e.g., 2023-10-25 10:00:00

    @Column(nullable = false)
    private LocalDateTime slotEnd;   // e.g., 2023-10-25 10:30:00

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @Column(nullable = false)
    @Builder.Default
    private boolean isBooked = false;

    public SlotsDto toSlotsDto(ReservationSlot slot) {
        return SlotsDto.builder()
                .deskId(slot.deskId)
                .startTime(slot.slotStart)
                .endTime(slot.slotEnd)
                .build();
    }
}