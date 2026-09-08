package com.enoca.flowpilot.service.policy;

import com.enoca.flowpilot.core.entities.Request;
import com.enoca.flowpilot.core.enums.RequestType;

public interface ApprovalPolicy {

    // Bu kural motorunun hangi talep türünü desteklediğini bildirir
    boolean supports(RequestType requestType);

    //Talebe özel onay adımlarını otomatik olarak üretir
    void generateApprovalSteps(Request request);
}
