package com.enoca.flowpilot.service.policy.impl;

import com.enoca.flowpilot.core.entities.Request;
import com.enoca.flowpilot.core.enums.RequestType;
import com.enoca.flowpilot.service.ApprovalService;
import com.enoca.flowpilot.service.policy.BaseApprovalPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SoftwareLicenseApprovalPolicy extends BaseApprovalPolicy {

    private final ApprovalService approvalService;

    @Override
    public boolean supports(RequestType requestType) {
        return RequestType.SOFTWARE_LICENSE.equals(requestType);
    }

    @Override
    public void validateDetails(Request request) {
        String softwareName = getDetailValue(request, "softwareName");
        if (softwareName == null || softwareName.isBlank()) {
            throw new IllegalArgumentException("Yazılım lisansı talebi için 'softwareName' alanı zorunludur.");
        }
    }

    @Override
    public void applyPolicy(Request request) {
        Long managerId = (request.getEmployee().getManager() != null)
                ? request.getEmployee().getManager().getId()
                : null;
        approvalService.createApprovalStep(request, "MANAGER", managerId, 1);
        approvalService.createApprovalStep(request, "IT_SUPPORT", null, 2);
    }
}