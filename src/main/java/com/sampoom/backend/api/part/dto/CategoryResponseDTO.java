package com.sampoom.backend.api.part.dto;

import com.sampoom.backend.api.part.entity.Category;
import lombok.Getter;

@Getter
public class CategoryResponseDTO {

    private Long categoryId;
    private String name;

    public CategoryResponseDTO(Category category) {
        this.categoryId = category.getId();
        this.name = category.getName();
    }
}
