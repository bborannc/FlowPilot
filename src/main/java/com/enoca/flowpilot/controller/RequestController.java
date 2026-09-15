package com.enoca.flowpilot.controller;

import com.enoca.flowpilot.dto.request.CreateRequestDto;
import com.enoca.flowpilot.dto.response.RequestResponseDto;
import com.enoca.flowpilot.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    // 1. Taslak talep oluşturma (DRAFT)
    @PostMapping
    public ResponseEntity<RequestResponseDto> createDraft(@RequestBody CreateRequestDto dto) {
        RequestResponseDto response = requestService.createDraft(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 2. Taslak talebi onaya sunma (IN_APPROVAL)
    @PostMapping("/{id}/submit")
    public ResponseEntity<RequestResponseDto> submitRequest(@PathVariable Long id) {
        RequestResponseDto response = requestService.submitRequest(id);
        return ResponseEntity.ok(response);
    }

    // 3. Tekil talep detayı
    @GetMapping("/{id}")
    public ResponseEntity<RequestResponseDto> getRequestDetails(@PathVariable Long id) {
        RequestResponseDto response = requestService.getRequestDetails(id);
        return ResponseEntity.ok(response);
    }

    // 4. Kullanıcının kendi taleplerini listelemesi
    @GetMapping("/my-requests")
    public ResponseEntity<List<RequestResponseDto>> getMyRequests(@RequestParam Long employeeId) {
        List<RequestResponseDto> responses = requestService.getMyRequests(employeeId);
        return ResponseEntity.ok(responses);
    }
}