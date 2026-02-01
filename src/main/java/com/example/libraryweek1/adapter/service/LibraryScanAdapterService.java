package com.example.libraryweek1.adapter.service;


import com.example.libraryweek1.adapter.dto.LibraryScanDto;
import com.example.libraryweek1.adapter.dto.LibraryScanResponseDto;

import java.util.concurrent.CompletableFuture;

public interface LibraryScanAdapterService {
    /**
     * Process a library scan event
     * @param scanDto The scan data containing student ID, timestamp, and scan type
     * @return LibraryScanResponseDto containing check-in status, break status, and remaining break minutes
     */
    CompletableFuture<LibraryScanResponseDto> processLibraryScan(LibraryScanDto scanDto);
} 