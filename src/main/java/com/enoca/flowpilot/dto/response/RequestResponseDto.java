package com.enoca.flowpilot.dto.response;

import com.enoca.flowpilot.core.enums.RequestPriority;
import com.enoca.flowpilot.core.enums.RequestStatus;
import com.enoca.flowpilot.core.enums.RequestType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestResponseDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private RequestType requestType;
    private RequestStatus status;
    private RequestPriority priority;
    private LocalDateTime createdAt;
    private Map<String, String> details;
    private List<ApprovalStepSummaryDto> steps;
}