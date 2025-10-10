package com.sampoom.backend.api.dto;

import com.sampoom.backend.api.domain.Category;
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
