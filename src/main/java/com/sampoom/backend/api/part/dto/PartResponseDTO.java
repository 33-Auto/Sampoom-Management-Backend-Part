package com.sampoom.backend.api.part.dto;

import com.sampoom.backend.api.part.entity.Part;
import lombok.Getter;

@Getter
public class PartResponseDTO {

    private Long partId;
    private String name;
    private String code;
    private Long groupId;

    public PartResponseDTO(Part part) {
        this.partId = part.getId();
        this.name = part.getName();
        this.code = part.getCode();
        this.groupId = part.getPartGroup().getId();
    }
}
