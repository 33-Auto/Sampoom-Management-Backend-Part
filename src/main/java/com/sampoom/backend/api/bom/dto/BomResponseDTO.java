package com.sampoom.backend.api.bom.dto;

import com.sampoom.backend.api.bom.entity.Bom;
import com.sampoom.backend.api.bom.entity.BomMaterial;
import com.sampoom.backend.api.part.entity.Part;
import com.sampoom.backend.api.part.entity.PartCategory;
import com.sampoom.backend.api.part.entity.PartGroup;
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
    private String bomCode;

    private Long partId;
    private String partName;
    private String partCode;

    private Long categoryId;
    private String categoryName;

    private Long groupId;
    private String groupName;

    private String version;
    private String status;
    private String complexity;


    private int componentCount;  // 원자재 종류 수
    private Long totalQuantity;  // 총 수량
    private Long totalCost;  // 총 금액
    private List<BomMaterialResponse> materials;  // 자재 구성 목록
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BomResponseDTO from(Bom bom) {
        Part part = bom.getPart();
        PartGroup group = part.getPartGroup();
        PartCategory category = (group != null) ? group.getCategory() : null;

        List<BomMaterialResponse> materials = bom.getMaterials().stream()
                .sorted(Comparator.comparing(bm -> bm.getMaterial().getName()))
                .map(BomMaterialResponse::from)
                .collect(Collectors.toList());

        // 실시간 계산
        int componentCount = materials.size();
        long totalQuantity = materials.stream()
                .mapToLong(BomMaterialResponse::getQuantity)
                .sum();

        return BomResponseDTO.builder()
                .id(bom.getId())
                .bomCode(bom.getBomCode())
                .partId(bom.getPart().getId())
                .partName(bom.getPart().getName())
                .partCode(bom.getPart().getCode())
                .version("v" + bom.getVersion())
                .categoryId(category != null ? category.getId() : null)
                .categoryName(category != null ? category.getName() : null)
                .groupId(group != null ? group.getId() : null)
                .groupName(group != null ? group.getName() : null)
                .status(bom.getStatus().name())
                .complexity(bom.getComplexity().name())
                .componentCount(componentCount)
                .totalQuantity(totalQuantity)
                .totalCost(bom.getTotalCost())
                .materials(materials)
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
        private Long standardCost;
        private Long total;  // 단가 * 수량

        public static BomMaterialResponse from(BomMaterial bm) {
            Long cost = bm.getMaterial().getStandardCost() != null
                    ? bm.getMaterial().getStandardCost()
                    : 0L;
            Long total = cost * bm.getQuantity();

            return BomMaterialResponse.builder()
                    .materialId(bm.getMaterial().getId())
                    .materialName(bm.getMaterial().getName())
                    .materialCode(bm.getMaterial().getMaterialCode())
                    .unit(bm.getMaterial().getMaterialUnit())
                    .quantity(bm.getQuantity())
                    .standardCost(cost)
                    .total(total)
                    .build();
        }
    }
}
