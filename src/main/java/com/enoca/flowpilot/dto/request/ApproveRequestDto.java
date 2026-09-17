package com.enoca.flowpilot.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ApproveRequestDto {
    private Long approverId;
    private String description;
}
