package com.enoca.flowpilot.dto.response;

import com.enoca.flowpilot.core.enums.ApprovalAction;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalHistoryResponseDto {
    private Long id;
    private String employeeName;
    private ApprovalAction action;
    private String description;
    private LocalDateTime actionDate;
}