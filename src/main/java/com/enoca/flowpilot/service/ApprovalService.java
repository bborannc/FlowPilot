package com.enoca.flowpilot.service;

import com.enoca.flowpilot.core.entities.ApprovalHistory;
import com.enoca.flowpilot.core.entities.ApprovalStep;
import com.enoca.flowpilot.core.entities.Request;
import com.enoca.flowpilot.core.enums.ApprovalAction;

import java.util.List;

public interface ApprovalService {
    void createApprovalStep(Request request, String roleName, Long assignedEmployeeId, int stepOrder);
    void recordHistory(Request request, Long employeeId, ApprovalAction action, String description);
    List<ApprovalStep> getPendingStepsForApprover(Long employeeId);
    List<ApprovalHistory> getRequestHistory(Long requestId);
}