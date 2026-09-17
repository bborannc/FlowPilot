package com.enoca.flowpilot.dto.response;

import com.enoca.flowpilot.core.enums.ApprovalStepStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalStepResponseDto {
    private Long stepId;
    private Long requestId;
    private String requestType;
    private String requesterName;
    private int stepOrder;
    private ApprovalStepStatus status;
    private String roleName;
    private LocalDateTime requestCreatedAt;
}