package com.enoca.flowpilot.mapper;

import com.enoca.flowpilot.core.entities.Request;
import com.enoca.flowpilot.core.entities.RequestDetail;
import com.enoca.flowpilot.dto.response.RequestResponseDto;

import java.util.HashMap;
import java.util.Map;

public final class RequestMapper {

    private RequestMapper() {
    }

    public static RequestResponseDto toDto(Request request) {
        if (request == null) {
            return null;
        }

        Map<String, String> detailsMap = new HashMap<>();
        if (request.getDetails() != null) {
            for (RequestDetail detail : request.getDetails()) {
                detailsMap.put(detail.getKey(), detail.getValue());
            }
        }

        return RequestResponseDto.builder()
                .id(request.getId())
                .employeeId(request.getEmployee() != null ? request.getEmployee().getId() : null)
                .employeeName(request.getEmployee() != null ? request.getEmployee().getName() : null)
                .requestType(request.getRequestType())
                .status(request.getStatus())
                .priority(request.getPriority())
                .createdAt(request.getCreatedAt())
                .details(detailsMap)
                .build();
    }
}