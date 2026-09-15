package com.enoca.flowpilot.service;

import com.enoca.flowpilot.dto.request.CreateRequestDto;
import com.enoca.flowpilot.dto.response.RequestResponseDto;

import java.util.List;

public interface RequestService {
    // 1. Yalnızca DRAFT statüsünde talep oluşturur (Onay akışı üretilmez)
    RequestResponseDto createDraft(CreateRequestDto dto);

    // 2. Draft talebi onaya sunar (Statü IN_APPROVAL olur, adımlar üretilir)
    RequestResponseDto submitRequest(Long requestId);

    // 3. Talep detaylarını getirir
    RequestResponseDto getRequestDetails(Long requestId);

    // 4. Kullanıcının taleplerini listeler
    List<RequestResponseDto> getMyRequests(Long employeeId);
}