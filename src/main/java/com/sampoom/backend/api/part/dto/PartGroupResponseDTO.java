package com.sampoom.backend.api.part.dto;

import com.sampoom.backend.api.part.entity.PartGroup;
import lombok.Getter;

@Getter
public class PartGroupResponseDTO {

    private Long groupId;
    private String groupName;
    private Long categoryId;
    private String categoryName;

    public PartGroupResponseDTO(PartGroup partGroup) {
        this.groupId = partGroup.getId();
        this.groupName = partGroup.getName();
        this.categoryId = partGroup.getCategory().getId();
        this.categoryName = partGroup.getCategory().getName();
    }
}
