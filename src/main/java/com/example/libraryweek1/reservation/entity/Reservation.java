package com.example.libraryweek1.reservation.entity;

import com.example.libraryweek1.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer roomId; // 1,2,3

    @Column(nullable = false)
    private Integer deskId;// 11,12,13 - 21,22,23 - 31,32,33

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Column
    private Long maxBreakTime;

    @Column
    private String cancellationReason;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // 0 at first, if user is on break add every minute on break
    @Column(nullable = false)
    private Long breakTime;

    @Column(nullable = false)
    private boolean onBreak;

    // The link to the locked slots
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = false)
    @Builder.Default
    private List<ReservationSlot> slots = new ArrayList<>();


    public void startBreak() {
        this.onBreak = true;
    }

    public void endBreak() {
        this.onBreak = false;
    }
    public boolean isOnBreak() {
        return this.onBreak;
    }

    // Helper method to add slots easily
    public void addSlot(ReservationSlot slot) {
        slots.add(slot);
        slot.setReservation(this);
    }
} 