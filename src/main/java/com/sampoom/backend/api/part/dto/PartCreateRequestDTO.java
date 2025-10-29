package com.sampoom.backend.api.part.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PartCreateRequestDTO {

    @NotNull(message = "소속될 그룹 ID는 필수입니다.")
    private Long groupId;

    @NotBlank(message = "부품 이름은 필수입니다.")
    private String name;

    @NotBlank(message = "기준 단위는 필수입니다.")
    private String partUnit;

    @NotNull(message = "기준 개수는 필수입니다.")
    private Integer baseQuantity;

    @NotNull(message = "리드 타임은 필수입니다.")
    private Integer leadTime;
}
