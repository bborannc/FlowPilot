package com.enoca.flowpilot.service.policy.impl;

import com.enoca.flowpilot.core.entities.Request;
import com.enoca.flowpilot.core.enums.RequestType;
import com.enoca.flowpilot.service.ApprovalService;
import com.enoca.flowpilot.service.policy.ApprovalPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TechnicalSupportApprovalPolicy implements ApprovalPolicy {

    private final ApprovalService approvalService;

    @Override
    public boolean supports(RequestType requestType) {
        return requestType == RequestType.TECHNICAL_SUPPORT;
    }

    @Override
    public void generateApprovalSteps(Request request) {
        approvalService.createApprovalStep(request, "HR", null, 1);
    }
}