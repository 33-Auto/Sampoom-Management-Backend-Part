package com.sampoom.backend.api.material.dto;

import com.sampoom.backend.api.material.entity.MaterialCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialCategoryResponseDTO {
    private Long id;
    private String name;
    private String code;

    public MaterialCategoryResponseDTO(MaterialCategory category) {
        this.id = category.getId();
        this.name = category.getName();
        this.code = category.getCode();
    }
}
