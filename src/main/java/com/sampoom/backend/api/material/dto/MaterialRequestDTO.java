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
}
