package com.enoca.flowpilot.controller;

import com.enoca.flowpilot.dto.request.CreateRequestDto;
import com.enoca.flowpilot.dto.response.RequestResponseDto;
import com.enoca.flowpilot.service.RequestService;
import jakarta.validation.Valid;
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

    @PostMapping
    public ResponseEntity<RequestResponseDto> createDraft(@Valid @RequestBody CreateRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(requestService.createDraft(dto));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<RequestResponseDto> submitRequest(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.submitRequest(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequestResponseDto> getRequestDetails(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.getRequestDetails(id));
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<RequestResponseDto>> getMyRequests(@RequestParam Long employeeId) {
        return ResponseEntity.ok(requestService.getMyRequests(employeeId));
    }
}