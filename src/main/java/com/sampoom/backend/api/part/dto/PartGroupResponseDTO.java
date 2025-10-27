package com.sampoom.backend.api.part.dto;

import com.sampoom.backend.api.part.entity.Group;
import lombok.Getter;

@Getter
public class PartGroupResponseDTO {

    private Long groupId;
    private String name;
    private Long categoryId;

    public PartGroupResponseDTO(Group partGroup) {
        this.groupId = partGroup.getId();
        this.name = partGroup.getName();
        this.categoryId = partGroup.getCategory().getId();
    }
}
