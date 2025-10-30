package com.sampoom.backend.api.part.dto;

import com.sampoom.backend.api.part.entity.Part;
import lombok.Getter;

@Getter
public class PartResponseDTO {

    private Long partId;
    private String name;
    private String code;

    private String partUnit;
    private Integer baseQuantity;
    private Integer leadTime;

    private Long groupId;
    private String groupName;

    public PartResponseDTO(Part part) {
        this.partId = part.getId();
        this.name = part.getName();
        this.code = part.getCode();

        this.partUnit = part.getPartUnit();
        this.baseQuantity = part.getBaseQuantity();
        this.leadTime = part.getLeadTime();

        if (part.getPartGroup() != null) {
            this.groupId = part.getPartGroup().getId();
            this.groupName = part.getPartGroup().getName();
        }
    }
}
