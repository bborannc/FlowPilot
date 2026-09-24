package com.enoca.flowpilot.dto.response;

import com.enoca.flowpilot.core.enums.ApprovalStepStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApprovalStepSummaryDto {
    private Long id;
    private Integer stepOrder;
    private ApprovalStepStatus status;
    private String assignedRoleName;
    private String assignedEmployeeName;
}