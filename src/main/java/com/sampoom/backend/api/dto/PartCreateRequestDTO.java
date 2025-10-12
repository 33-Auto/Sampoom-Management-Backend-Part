package com.sampoom.backend.api.dto;

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

    @NotBlank(message = "부품 코드는 필수입니다.")
    private String code;

    @NotBlank(message = "부품 이름은 필수입니다.")
    private String name;
}
