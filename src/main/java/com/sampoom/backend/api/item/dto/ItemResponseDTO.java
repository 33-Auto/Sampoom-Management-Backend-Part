package com.sampoom.backend.api.item.dto;

import com.sampoom.backend.api.material.dto.MaterialResponseDTO;
import com.sampoom.backend.api.part.dto.PartListResponseDTO;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemResponseDTO {
    private String type;      // 원자재 / 부품
    private String code;
    private String name;
    private String categoryName;
    private String groupName;
    private String unit;
    private String status;

    public static ItemResponseDTO ofMaterial(MaterialResponseDTO m) {
        return ItemResponseDTO.builder()
                .type("원자재")
                .code(m.getMaterialCode())
                .name(m.getName())
                .categoryName(m.getMaterialCategoryName())
                .groupName(null)  // 자재는 그룹 없음
                .unit(m.getMaterialUnit())
                .build();
    }

    public static ItemResponseDTO ofPart(PartListResponseDTO p) {
        return ItemResponseDTO.builder()
                .type("부품")
                .code(p.getCode())
                .name(p.getName())
                .categoryName(p.getCategoryName())
                .groupName(p.getGroupName())
                .unit(null)
                .status(p.getStatus())
                .build();
    }
}
