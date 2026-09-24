package com.enoca.flowpilot.service.policy;

import com.enoca.flowpilot.core.entities.Request;
import com.enoca.flowpilot.core.enums.RequestType;

public interface ApprovalPolicy {
    boolean supports(RequestType requestType);
    void validateDetails(Request request);
    void applyPolicy(Request request);
}