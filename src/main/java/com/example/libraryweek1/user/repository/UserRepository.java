package com.example.libraryweek1.user.repository;

import com.example.libraryweek1.user.entity.Role;
import com.example.libraryweek1.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByStudentId(Long studentId);
    boolean existsByEmail(String email);
    boolean existsByStudentId(Long studentId);

    boolean existsByRole(Role role);
} 