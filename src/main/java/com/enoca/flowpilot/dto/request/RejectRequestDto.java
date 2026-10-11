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

    @NotBlank(message = "Reddetme işleminde açıklama zorunludur.")
    private String description;
}
