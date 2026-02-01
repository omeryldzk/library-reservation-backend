package com.example.libraryweek1.adapter.controller;


import com.example.libraryweek1.adapter.dto.LibraryScanDto;
import com.example.libraryweek1.adapter.dto.LibraryScanResponseDto;
import com.example.libraryweek1.adapter.service.LibraryScanAdapterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/adapter/library")
@RequiredArgsConstructor
public class LibraryScanController {

    private final LibraryScanAdapterService libraryScanAdapterService;

    @PostMapping("/scan")
    public ResponseEntity<CompletableFuture<LibraryScanResponseDto>> handleLibraryScan(@Valid @RequestBody LibraryScanDto scanDto) {
        CompletableFuture<LibraryScanResponseDto> response = libraryScanAdapterService.processLibraryScan(scanDto);
        return ResponseEntity.ok(response);
    }
} 