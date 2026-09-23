package com.enoca.flowpilot.service.policy.impl;

import com.enoca.flowpilot.core.entities.Request;
import com.enoca.flowpilot.core.enums.RequestType;
import com.enoca.flowpilot.service.ApprovalService;
import com.enoca.flowpilot.service.policy.BaseApprovalPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TechnicalSupportApprovalPolicy extends BaseApprovalPolicy {

    private final ApprovalService approvalService;

    @Override
    public boolean supports(RequestType requestType) {
        return RequestType.TECHNICAL_SUPPORT.equals(requestType);
    }

    @Override
    public void validateDetails(Request request) {
        String description = getDetailValue(request, "description");
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Teknik destek talebi için 'description' alanı zorunludur.");
        }
    }

    @Override
    public void applyPolicy(Request request) {
        approvalService.createApprovalStep(request, "IT_SUPPORT", null, 1);
    }
}