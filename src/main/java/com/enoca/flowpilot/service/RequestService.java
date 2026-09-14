package com.enoca.flowpilot.service;

import com.enoca.flowpilot.core.entities.Request;
import com.enoca.flowpilot.core.enums.RequestPriority;
import com.enoca.flowpilot.core.enums.RequestType;
import com.enoca.flowpilot.dto.request.CreateRequestDto;
import com.enoca.flowpilot.dto.response.RequestResponseDto;

import java.util.List;
import java.util.Map;

public interface RequestService {
    Request createAndSubmitRequest(Long employeeId, RequestType type, RequestPriority priority, Map<String, String> details);
    Request getRequestById(Long id);
    List<Request> getRequestsByEmployee(Long employeeId);

    RequestResponseDto createAndSubmit(CreateRequestDto dto);

    // 2. Mevcut bir taslak talebi onaya sunar
    RequestResponseDto submitRequest(Long requestId);

    // 3. Tekil talep detayı
    RequestResponseDto getRequestDetails(Long requestId);

    // 4. Kullanıcının kendi taleplerini listeleme
    List<RequestResponseDto> getMyRequests(Long employeeId);
}