package com.sampoom.backend.api.material.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialRequestDTO {
    private String name;
    private Long materialCategoryId;
    private String materialUnit;
    private Integer baseQuantity;  // 기준단위
    private Integer standardQuantity;  // 기준 수량
    private Integer leadTime;      // 리드타임
    private Long standardCost;         // 표준단가
}
