package com.sampoom.backend.api.part.dto;

import com.sampoom.backend.api.part.entity.PartCategory;
import lombok.Getter;

@Getter
public class PartCategoryResponseDTO {

    private Long categoryId;
    private String categoryName;
    private String name;

    public PartCategoryResponseDTO(PartCategory category) {
        this.categoryId = category.getId();
        this.categoryName = category.getName();
        this.name = category.getName();
    }
}
