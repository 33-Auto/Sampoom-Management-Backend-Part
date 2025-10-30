package com.sampoom.backend.api.bom.dto;

import com.sampoom.backend.api.bom.entity.Bom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BomDetailResponseDTO {
    private Long id;
    private String partName;
    private String partCode;
    private Long partId;
    private String status;
    private String complexity;
    private List<BomMaterialDTO> materials;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BomMaterialDTO {
        private Long id;
        private Long materialId;
        private String materialName;
        private String materialCode;
        private String unit;
        private Long quantity;
    }

    public static BomDetailResponseDTO from(Bom bom) {
        List<BomMaterialDTO> materialDtos = bom.getMaterials().stream()
                .map(material -> BomMaterialDTO.builder()
                        .id(material.getId())
                        .materialId(material.getMaterial().getId())
                        .materialName(material.getMaterial().getName())
                        .materialCode(material.getMaterial().getMaterialCode())
                        .unit(material.getMaterial().getMaterialUnit())
                        .quantity(material.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return BomDetailResponseDTO.builder()
                .id(bom.getId())
                .partId(bom.getPart().getId())
                .partName(bom.getPart().getName())
                .partCode(bom.getPart().getCode())
                .status(bom.getStatus().name())
                .complexity(bom.getComplexity().name())
                .materials(materialDtos)
                .createdAt(bom.getCreatedAt())
                .updatedAt(bom.getUpdatedAt())
                .build();
    }
}
