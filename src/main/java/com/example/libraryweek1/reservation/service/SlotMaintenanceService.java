package com.example.libraryweek1.reservation.service;

import com.example.libraryweek1.reservation.entity.ReservationSlot;
import com.example.libraryweek1.reservation.repository.ReservationSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlotMaintenanceService {

    private final ReservationSlotRepository slotRepository;

    // CONFIGURATION (Ideally move these to application.properties)
    private static final int DAYS_TO_PREGENERATE = 5;
    private static final LocalTime OPENING_TIME = LocalTime.of(9, 0);
    private static final LocalTime CLOSING_TIME = LocalTime.of(17, 0);
    private static final int SLOT_DURATION_MINUTES = 30;
    private static final List<Integer> DESK_IDS = List.of(11,12,13,14,21,22,23,24,31,32,33,34); // Example desks

    /**
     * WORKER 1 (Part A): Runs ONLY on Application Startup.
     * Generates slots for the next 5 days (Today + 4 days).
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onStartup() {
        log.info("Application started. Checking and populating slots for the next {} days...", DAYS_TO_PREGENERATE);

        LocalDate today = LocalDate.now();
        for (int i = 0; i < DAYS_TO_PREGENERATE; i++) {
            generateSlotsForDate(today.plusDays(i));
        }

        log.info("Startup slot population finished.");
    }

    /**
     * WORKER 1 (Part B): Runs every day at Midnight (00:00:00).
     * Populates the 5th day out (maintaining the sliding window).
     */
    @Scheduled(cron = "0 0 0 * * *") // Seconds Minutes Hours Day Month Year
    @Transactional
    public void generateNewDaySlots() {
        // If today is Monday, we already have slots up to Friday.
        // We need to generate slots for Saturday (Today + 5 days logic depends on how you count, usually +4 is the 5th day inclusive, but let's do +DAYS_TO_PREGENERATE -1 or just extend the window).

        // Strategy: Always ensure the day at index "DAYS_TO_PREGENERATE" exists.
        LocalDate targetDate = LocalDate.now().plusDays(DAYS_TO_PREGENERATE - 1); // e.g. Day 5

        log.info("Midnight Worker: Generating slots for {}", targetDate);
        generateSlotsForDate(targetDate);
    }

    /**
     * WORKER 2: Runs every day at Midnight (00:00:00).
     * Deletes slots that ended before now.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupOldSlots() {
        log.info("Midnight Worker: Cleaning up old slots...");

        // Delete anything that ended before Now
        slotRepository.deleteBySlotEndBefore(LocalDateTime.now());

        log.info("Old slots deleted.");
    }

    // --- Helper Logic ---

    private void generateSlotsForDate(LocalDate date) {
        List<ReservationSlot> slotsBatch = new ArrayList<>();
        LocalDateTime startTime = LocalDateTime.of(date, OPENING_TIME);
        LocalDateTime endTime = LocalDateTime.of(date, CLOSING_TIME);

        // Loop through desks
        for (Integer deskId : DESK_IDS) {
            LocalDateTime currentSlotStart = startTime;

            // Loop through time blocks (9:00 -> 17:00)
            while (currentSlotStart.isBefore(endTime)) {
                LocalDateTime currentSlotEnd = currentSlotStart.plusMinutes(SLOT_DURATION_MINUTES);

                // 1. Check if slot already exists (Idempotency)
                // This prevents crashing if you restart the app multiple times in one day
                if (!slotRepository.existsByDeskIdAndSlotStart(deskId, currentSlotStart)) {

                    ReservationSlot newSlot = ReservationSlot.builder()
                            .deskId(deskId)
                            .slotStart(currentSlotStart)
                            .slotEnd(currentSlotEnd)
                            .isBooked(false)
                            .reservation(null) // Empty slot
                            .build();

                    slotsBatch.add(newSlot);
                }

                currentSlotStart = currentSlotEnd;
            }
        }

        // Batch save for performance
        if (!slotsBatch.isEmpty()) {
            slotRepository.saveAll(slotsBatch);
            log.info("Generated {} slots for date {}", slotsBatch.size(), date);
        } else {
            log.info("Slots for {} already exist. Skipping.", date);
        }
    }
}