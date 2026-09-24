package com.enoca.flowpilot.mapper;

import com.enoca.flowpilot.core.entities.ApprovalStep;
import com.enoca.flowpilot.core.entities.Request;
import com.enoca.flowpilot.core.entities.RequestDetail;
import com.enoca.flowpilot.dto.response.ApprovalStepSummaryDto;
import com.enoca.flowpilot.dto.response.RequestResponseDto;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        List<ApprovalStepSummaryDto> stepDtos = Collections.emptyList();
        if (request.getSteps() != null && !request.getSteps().isEmpty()) {
            stepDtos = request.getSteps().stream()
                    .sorted(Comparator.comparing(ApprovalStep::getStepOrder))
                    .map(step -> ApprovalStepSummaryDto.builder()
                            .id(step.getId())
                            .stepOrder(step.getStepOrder())
                            .status(step.getStatus())
                            .assignedRoleName(step.getAssignedRole() != null ? step.getAssignedRole().getName() : null)
                            .assignedEmployeeName(step.getAssignedEmployee() != null ? step.getAssignedEmployee().getName() : null)
                            .build())
                    .collect(Collectors.toList());
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
                .steps(stepDtos)
                .build();
    }
}