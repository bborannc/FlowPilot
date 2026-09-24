package com.enoca.flowpilot.dto.request;

import com.enoca.flowpilot.core.enums.RequestPriority;
import com.enoca.flowpilot.core.enums.RequestType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRequestDto {

    @NotNull(message = "Çalışan ID boş olamaz.")
    private Long employeeId;

    @NotNull(message = "Talep tipi boş olamaz.")
    private RequestType requestType;

    @NotNull(message = "Öncelik seviyesi boş olamaz.")
    private RequestPriority priority;

    private Map<String, String> details;
}