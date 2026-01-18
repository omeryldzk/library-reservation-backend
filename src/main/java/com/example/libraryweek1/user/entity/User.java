package com.example.libraryweek1.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, unique = true)
    private Long studentId;

    @Column(nullable = false)
    @Builder.Default
    private Integer libraryScore = 100;

    @Column(nullable = false)
    @Builder.Default
    private Integer successfulCompletionsStreak = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer noShowStreak = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer breakViolationStreak = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.STUDENT;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = false;
} 