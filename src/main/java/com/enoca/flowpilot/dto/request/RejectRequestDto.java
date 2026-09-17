package com.enoca.flowpilot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RejectRequestDto {
    @NotNull(message = "Onaylayıcı ID boş olamaz.")
    private Long approverId;

    @NotBlank(message = "Reddetme işleminde açıklama zorunludur.")
    private String description;
}
