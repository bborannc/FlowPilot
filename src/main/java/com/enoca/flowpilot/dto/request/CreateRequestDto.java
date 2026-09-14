package com.enoca.flowpilot.dto.request;

import com.enoca.flowpilot.core.enums.RequestPriority;
import com.enoca.flowpilot.core.enums.RequestType;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRequestDto {
    private Long employeeId;
    private RequestType requestType;
    private RequestPriority priority;
    private Map<String, String> details; // örn: {"amount": "7500", "description": "Monitör alımı"}
}
