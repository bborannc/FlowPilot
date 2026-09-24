package com.enoca.flowpilot.service.policy;

import com.enoca.flowpilot.core.entities.Request;
import com.enoca.flowpilot.core.entities.RequestDetail;

public abstract class BaseApprovalPolicy implements ApprovalPolicy {

    protected String getDetailValue(Request request, String key) {
        if (request.getDetails() == null) {
            return null;
        }
        return request.getDetails().stream()
                .filter(d -> d.getKey().equalsIgnoreCase(key))
                .findFirst()
                .map(RequestDetail::getValue)
                .orElse(null);
    }
}