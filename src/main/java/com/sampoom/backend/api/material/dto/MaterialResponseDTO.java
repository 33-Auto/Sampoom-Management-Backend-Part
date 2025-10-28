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

    private Long materialCategoryId;
    private String materialCategoryName;

    public MaterialResponseDTO(Material material) {
        this.id = material.getId();
        this.name = material.getName();
        this.materialCode = material.getMaterialCode();
        this.materialUnit = material.getMaterialUnit();

        if (material.getMaterialCategory() != null) {
            this.materialCategoryId = material.getMaterialCategory().getId();
            this.materialCategoryName = material.getMaterialCategory().getName();
        }
    }
}
