package com.enoca.flowpilot.service;

import com.enoca.flowpilot.core.entities.Request;
import com.enoca.flowpilot.core.enums.ApprovalAction;
import com.enoca.flowpilot.dto.request.ApproveRequestDto;
import com.enoca.flowpilot.dto.request.RejectRequestDto;
import com.enoca.flowpilot.dto.response.ApprovalHistoryResponseDto;
import com.enoca.flowpilot.dto.response.ApprovalStepResponseDto;

import java.util.List;

public interface ApprovalService {
    void createApprovalStep(Request request, String roleName, Long assignedEmployeeId, int stepOrder);
    void recordHistory(Request request, Long employeeId, ApprovalAction action, String description);

    void approveStep(Long stepId, ApproveRequestDto dto);
    void rejectStep(Long stepId, RejectRequestDto dto);

    // Listeleme metotları
    List<ApprovalStepResponseDto> getPendingStepsForApprover(Long employeeId);
    List<ApprovalHistoryResponseDto> getRequestHistory(Long requestId);
}