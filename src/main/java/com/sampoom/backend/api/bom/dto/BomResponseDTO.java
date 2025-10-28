package com.sampoom.backend.api.bom.dto;

import com.sampoom.backend.api.bom.entity.Bom;
import com.sampoom.backend.api.bom.entity.BomMaterial;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BomResponseDTO {
    private Long id;
    private String partName;
    private String partCode;
    private Long partId;
    private List<BomMaterialResponse> materials;  // 자재 구성 목록
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BomResponseDTO from(Bom bom) {
        return BomResponseDTO.builder()
                .id(bom.getId())
                .partId(bom.getPart().getId())
                .partName(bom.getPart().getName())
                .partCode(bom.getPart().getCode())
                .materials(bom.getMaterials().stream()
                        .sorted(Comparator.comparing(bm -> bm.getMaterial().getName())) // 이름순 정렬 (선택사항)
                        .map(BomMaterialResponse::from)
                        .collect(Collectors.toList()))
                .createdAt(bom.getCreatedAt())
                .updatedAt(bom.getUpdatedAt())
                .build();
    }

    // 자재 응답 DTO 내부 클래스
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BomMaterialResponse {
        private Long materialId;
        private String materialName;
        private String materialCode;
        private String unit;
        private Long quantity;

        public static BomMaterialResponse from(BomMaterial bm) {
            return BomMaterialResponse.builder()
                    .materialId(bm.getMaterial().getId())
                    .materialName(bm.getMaterial().getName())
                    .materialCode(bm.getMaterial().getMaterialCode())
                    .unit(bm.getMaterial().getMaterialUnit())
                    .quantity(bm.getQuantity())
                    .build();
        }
    }
}
