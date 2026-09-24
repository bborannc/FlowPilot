package com.enoca.flowpilot.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApproveRequestDto {

    @NotNull(message = "Onaylayıcı çalışan ID boş olamaz.")
    private Long approverId;

    private String description;
}