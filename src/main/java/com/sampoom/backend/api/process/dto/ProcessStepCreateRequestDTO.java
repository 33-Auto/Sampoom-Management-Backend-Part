package com.sampoom.backend.api.process.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessStepCreateRequestDTO {

    @NotNull(message = "공정 순서를 입력하세요.")
    @Min(value = 1, message = "공정 순서는 1 이상이어야 합니다.")
    private Integer stepOrder;

    @NotBlank(message = "공정명을 입력하세요.")
    private String stepName;

    @NotNull(message = "작업장을 선택하세요.")
    private Long workCenterId;

    @NotNull @Min(0)
    private Integer setupMinutes;

    @NotNull @Min(0)
    private Integer processMinutes;

    @NotNull @Min(0)
    private Integer waitMinutes;
}

