package com.enoca.flowpilot.controller;

import com.enoca.flowpilot.dto.request.ApproveRequestDto;
import com.enoca.flowpilot.dto.request.RejectRequestDto;
import com.enoca.flowpilot.dto.response.ApprovalHistoryResponseDto;
import com.enoca.flowpilot.dto.response.ApprovalStepResponseDto;
import com.enoca.flowpilot.service.ApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    // 1. Onaycının bekleyen adımlarını listeleme
    @GetMapping("/pending")
    public ResponseEntity<List<ApprovalStepResponseDto>> getPendingSteps(@RequestParam Long employeeId) {
        List<ApprovalStepResponseDto> steps = approvalService.getPendingStepsForApprover(employeeId);
        return ResponseEntity.ok(steps);
    }

    // 2. Adımı onaylama
    @PostMapping("/{stepId}/approve")
    public ResponseEntity<String> approveStep(
            @PathVariable Long stepId,
            @RequestBody ApproveRequestDto dto) {
        approvalService.approveStep(stepId, dto);
        return ResponseEntity.ok("Talep adımı başarıyla onaylandı.");
    }

    // 3. Adımı reddetme (açıklama zorunlu)
    @PostMapping("/{stepId}/reject")
    public ResponseEntity<String> rejectStep(
            @PathVariable Long stepId,
            @Valid @RequestBody RejectRequestDto dto) {
        approvalService.rejectStep(stepId, dto);
        return ResponseEntity.ok("Talep reddedildi.");
    }

    // 4. Talebin geçmiş onay aksiyonlarını görüntüleme
    @GetMapping("/requests/{requestId}/history")
    public ResponseEntity<List<ApprovalHistoryResponseDto>> getRequestHistory(@PathVariable Long requestId) {
        List<ApprovalHistoryResponseDto> history = approvalService.getRequestHistory(requestId);
        return ResponseEntity.ok(history);
    }
}