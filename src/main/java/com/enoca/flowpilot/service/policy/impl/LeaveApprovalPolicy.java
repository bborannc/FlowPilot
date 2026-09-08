package com.enoca.flowpilot.service.policy.impl;

import com.enoca.flowpilot.core.entities.Request;
import com.enoca.flowpilot.core.entities.RequestDetail;
import com.enoca.flowpilot.core.enums.RequestType;
import com.enoca.flowpilot.service.ApprovalService;
import com.enoca.flowpilot.service.policy.ApprovalPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LeaveApprovalPolicy implements ApprovalPolicy {

    private final ApprovalService approvalService;

    @Override
    public boolean supports(RequestType requestType) {
        return requestType == RequestType.LEAVE;
    }

    @Override
    public void generateApprovalSteps(Request request) {
        Long managerId = (request.getEmployee().getManager() != null)
                ? request.getEmployee().getManager().getId()
                : null;

        approvalService.createApprovalStep(request, "MANAGER", managerId, 1);

        int totalDays = request.getDetails().stream()
                .filter(d -> "totalDays".equalsIgnoreCase(d.getKey()))
                .findFirst()
                .map(RequestDetail::getValue)
                .map(Integer::parseInt)
                .orElse(1);

        if (totalDays > 3) {
            approvalService.createApprovalStep(request, "HR", null, 2);
        }
    }
}