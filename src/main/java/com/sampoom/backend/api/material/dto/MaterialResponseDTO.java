package com.sampoom.backend.api.material.dto;

import com.sampoom.backend.api.material.entity.Material;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialResponseDTO {

    private Long id;
    private String name;
    private String materialCode;
    private String materialUnit;
    private Integer baseQuantity;  // 기준단위
    private Integer standardQuantity;  // 기준 수량
    private Integer leadTime;      // 리드타임

    private Long standardCost;         // 표준단가

    private Long materialCategoryId;
    private String materialCategoryName;

    public MaterialResponseDTO(Material material) {
        this.id = material.getId();
        this.name = material.getName();
        this.materialCode = material.getMaterialCode();
        this.materialUnit = material.getMaterialUnit();
        this.baseQuantity = material.getBaseQuantity();
        this.standardQuantity = material.getStandardQuantity();
        this.leadTime = material.getLeadTime();
        this.standardCost = material.getStandardCost();

        if (material.getMaterialCategory() != null) {
            this.materialCategoryId = material.getMaterialCategory().getId();
            this.materialCategoryName = material.getMaterialCategory().getName();
        }
    }
}
