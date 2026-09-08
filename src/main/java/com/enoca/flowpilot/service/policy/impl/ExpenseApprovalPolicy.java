package com.enoca.flowpilot.service.policy.impl;

import com.enoca.flowpilot.core.entities.Request;
import com.enoca.flowpilot.core.entities.RequestDetail;
import com.enoca.flowpilot.core.enums.RequestType;
import com.enoca.flowpilot.service.ApprovalService;
import com.enoca.flowpilot.service.policy.ApprovalPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class ExpenseApprovalPolicy implements ApprovalPolicy {

    private final ApprovalService approvalService;

    @Override
    public boolean supports(RequestType requestType) {
        return requestType == RequestType.EXPENSE;
    }

    @Override
    public void generateApprovalSteps(Request request) {
    // 1. Adım: Doğrudan çalışan yöneticisine atanan onay
        Long managerId = (request.getEmployee().getManager() != null)
                ? request.getEmployee().getManager().getId()
                : null;

        approvalService.createApprovalStep(request, "MANAGER", managerId, 1);

        // "amount" detayını çekelim
        BigDecimal amount = request.getDetails().stream()
                .filter(d -> "amount".equalsIgnoreCase(d.getKey()))
                .findFirst()
                .map(RequestDetail::getValue)
                .map(BigDecimal::new)
                .orElse(BigDecimal.ZERO);

        // 5000 TL üstüyse 2. Adım olarak Finans onayına gider
        if (amount.compareTo(new BigDecimal("5000")) > 0) {
            approvalService.createApprovalStep(request, "FINANCE", null, 2);
        }
    }
}
