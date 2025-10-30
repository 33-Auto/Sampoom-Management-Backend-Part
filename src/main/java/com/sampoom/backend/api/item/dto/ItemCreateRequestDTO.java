package com.sampoom.backend.api.item.dto;

import com.sampoom.backend.api.item.enums.ItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ItemCreateRequestDTO {

    @NotNull(message = "품목 유형은 필수입니다.")
    private ItemType itemType; // PART or MATERIAL

    @NotBlank(message = "품목명은 필수입니다.")
    private String name;

    @NotNull(message = "카테고리 ID는 필수입니다.")
    private Long categoryId;

    private Long groupId; // 자재는 null 가능

    @NotBlank(message = "기본 단위는 필수입니다.")
    private String unit;

    @NotNull(message = "기준 개수는 필수입니다.")
    private Integer baseQuantity;

    @NotNull(message = "리드타임은 필수입니다.")
    @PositiveOrZero(message = "리드타임은 0 이상이어야 합니다.")
    private Integer leadTime;
}
