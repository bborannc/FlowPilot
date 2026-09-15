package com.enoca.flowpilot.service.policy;

import com.enoca.flowpilot.core.enums.RequestType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApprovalPolicyRegistry {

    private final List<ApprovalPolicy> policies;

    public ApprovalPolicyRegistry(List<ApprovalPolicy> policies) {
        this.policies = policies;
    }

    public ApprovalPolicy getPolicy(RequestType requestType) {
        return policies.stream()
                .filter(policy -> policy.supports(requestType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Bu talep tipi için tanımlı bir onay politikası bulunamadı: " + requestType));
    }
}