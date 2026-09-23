package com.enoca.flowpilot.service.policy.impl;

import com.enoca.flowpilot.core.entities.Request;
import com.enoca.flowpilot.core.enums.RequestType;
import com.enoca.flowpilot.service.ApprovalService;
import com.enoca.flowpilot.service.policy.BaseApprovalPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SalaryAdvanceApprovalPolicy extends BaseApprovalPolicy {

    private final ApprovalService approvalService;

    @Override
    public boolean supports(RequestType requestType) {
        return RequestType.SALARY_ADVANCE.equals(requestType);
    }

    @Override
    public void validateDetails(Request request) {
        String amountStr = getDetailValue(request, "amount");
        if (amountStr == null || amountStr.isBlank()) {
            throw new IllegalArgumentException("Maaş avansı talebi için 'amount' alanı zorunludur.");
        }
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                throw new IllegalArgumentException("Avans tutarı 0'dan büyük olmalıdır.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'amount' geçerli bir sayı olmalıdır.");
        }
    }

    @Override
    public void applyPolicy(Request request) {
        String amountStr = getDetailValue(request, "amount");
        double amount = (amountStr != null) ? Double.parseDouble(amountStr) : 0;

        Long managerId = (request.getEmployee().getManager() != null)
                ? request.getEmployee().getManager().getId()
                : null;
        approvalService.createApprovalStep(request, "MANAGER", managerId, 1);

        if (amount > 10000) {
            approvalService.createApprovalStep(request, "FINANCE", null, 2);
        }
    }
}