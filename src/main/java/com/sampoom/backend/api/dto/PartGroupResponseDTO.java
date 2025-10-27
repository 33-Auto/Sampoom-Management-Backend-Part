package com.sampoom.backend.api.dto;

import com.sampoom.backend.api.domain.PartGroup;
import lombok.Getter;

@Getter
public class PartGroupResponseDTO {

    private Long groupId;
    private String name;
    private Long categoryId;

    public PartGroupResponseDTO(PartGroup partGroup) {
        this.groupId = partGroup.getId();
        this.name = partGroup.getName();
        this.categoryId = partGroup.getCategory().getId();
    }
}
