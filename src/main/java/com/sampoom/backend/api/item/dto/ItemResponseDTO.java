package com.sampoom.backend.api.item.dto;

import com.sampoom.backend.api.material.dto.MaterialResponseDTO;
import com.sampoom.backend.api.part.dto.PartListResponseDTO;
import lombok.*;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemResponseDTO {
    private Long id;
    private String type;      // 원자재 / 부품
    private String code;
    private String name;

    private Long categoryId;
    private String categoryName;

    private Long groupId;
    private String groupName;

    private String unit;

    private Integer leadTime;
    private Integer baseQuantity;
    private Integer standardQuantity;

    private Long standardCost;

    public static ItemResponseDTO ofMaterial(MaterialResponseDTO m) {
        return ItemResponseDTO.builder()
                .id(m.getId())
                .type("원자재")
                .code(m.getMaterialCode())
                .name(m.getName())
                .categoryId(m.getMaterialCategoryId())
                .categoryName(m.getMaterialCategoryName())
                .groupName(null)  // 자재는 그룹 없음
                .unit(m.getMaterialUnit())
                .leadTime(m.getLeadTime())
                .baseQuantity(m.getBaseQuantity())
                .standardQuantity(m.getStandardQuantity())
                .standardCost(m.getStandardCost())
                .build();
    }

    public static ItemResponseDTO ofPart(PartListResponseDTO p) {
        return ItemResponseDTO.builder()
                .id(p.getPartId())
                .type("부품")
                .code(p.getCode())
                .name(p.getName())
                .categoryId(p.getCategoryId())
                .categoryName(p.getCategoryName())
                .groupId(p.getGroupId())
                .groupName(p.getGroupName())
                .unit(p.getPartUnit())
                .leadTime(p.getLeadTime())
                .baseQuantity(p.getBaseQuantity())
                .standardQuantity(p.getStandardQuantity())
                .standardCost(p.getStandardCost())
                .build();
    }
}
