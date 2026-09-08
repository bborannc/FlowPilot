package com.enoca.flowpilot.service;

import com.enoca.flowpilot.core.entities.Request;
import com.enoca.flowpilot.core.enums.RequestPriority;
import com.enoca.flowpilot.core.enums.RequestType;

import java.util.List;
import java.util.Map;

public interface RequestService {
    Request createAndSubmitRequest(Long employeeId, RequestType type, RequestPriority priority, Map<String, String> details);
    Request getRequestById(Long id);
    List<Request> getRequestsByEmployee(Long employeeId);
}