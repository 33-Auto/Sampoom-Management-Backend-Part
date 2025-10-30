package com.sampoom.backend.api.part.dto;

import com.sampoom.backend.api.part.entity.Part;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartListResponseDTO {

    private Long partId;
    private String code;
    private String name;
    private String status;

    private String partUnit;
    private Integer baseQuantity;
    private Integer leadTime;

    private Long groupId;
    private String groupName;
    private Long categoryId;
    private String categoryName;

    public PartListResponseDTO(Part part) {
        this.partId = part.getId();
        this.code = part.getCode();
        this.name = part.getName();
        this.status = part.getStatus().name();

        this.partUnit = part.getPartUnit();
        this.baseQuantity = part.getBaseQuantity();
        this.leadTime = part.getLeadTime();

        this.groupId = part.getPartGroup().getId();
        this.groupName = part.getPartGroup().getName();

        this.categoryId = part.getPartGroup().getCategory().getId();
        this.categoryName = part.getPartGroup().getCategory().getName();
    }
}
