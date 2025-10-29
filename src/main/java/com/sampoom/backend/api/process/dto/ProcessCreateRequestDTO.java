package com.sampoom.backend.api.process.dto;

import com.sampoom.backend.api.process.entity.ProcessStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessCreateRequestDTO {

    @NotNull(message = "부품 ID를 입력하세요.")
    private Long partId;

    @NotBlank(message = "버전을 입력하세요.")
    private String version;

    @NotNull(message = "상태를 입력하세요.")
    private ProcessStatus status;

    @NotEmpty(message = "공정 순서를 1개 이상 입력하세요.")
    @Valid
    private List<ProcessStepCreateRequestDTO> steps;
}

