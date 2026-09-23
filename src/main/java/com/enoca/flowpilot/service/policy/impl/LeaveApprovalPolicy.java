package com.enoca.flowpilot.service.policy.impl;

import com.enoca.flowpilot.core.entities.Request;
import com.enoca.flowpilot.core.enums.RequestType;
import com.enoca.flowpilot.service.ApprovalService;
import com.enoca.flowpilot.service.policy.BaseApprovalPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LeaveApprovalPolicy extends BaseApprovalPolicy {

    private final ApprovalService approvalService;

    @Override
    public boolean supports(RequestType requestType) {
        return RequestType.LEAVE.equals(requestType);
    }

    @Override
    public void validateDetails(Request request) {
        String totalDaysStr = getDetailValue(request, "totalDays");
        if (totalDaysStr == null || totalDaysStr.isBlank()) {
            throw new IllegalArgumentException("İzin talebi için 'totalDays' alanı zorunludur.");
        }
        try {
            int days = Integer.parseInt(totalDaysStr);
            if (days <= 0) {
                throw new IllegalArgumentException("İzin gün sayısı 0'dan büyük olmalıdır.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'totalDays' geçerli bir tam sayı olmalıdır.");
        }
    }

    @Override
    public void applyPolicy(Request request) {
        String totalDaysStr = getDetailValue(request, "totalDays");
        int totalDays = (totalDaysStr != null) ? Integer.parseInt(totalDaysStr) : 1;

        Long managerId = (request.getEmployee().getManager() != null)
                ? request.getEmployee().getManager().getId()
                : null;
        approvalService.createApprovalStep(request, "MANAGER", managerId, 1);

        if (totalDays > 3) {
            approvalService.createApprovalStep(request, "HR", null, 2);
        }
    }
}