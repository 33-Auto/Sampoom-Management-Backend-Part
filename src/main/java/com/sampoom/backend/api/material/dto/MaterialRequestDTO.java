package com.sampoom.backend.api.material.dto;

import com.sampoom.backend.api.part.entity.ProcurementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialRequestDTO {
    private String name;
    private Long materialCategoryId;
    private String materialUnit;
    private Integer baseQuantity;  // 기준단위
    private Integer leadTime;      // 리드타임
    private ProcurementType procurementType; // 조달유형 (구매/제작)
    private BigDecimal standardCost;         // 표준단가
}
