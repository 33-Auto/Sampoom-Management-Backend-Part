package com.sampoom.backend.api.bom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BomRequestDTO {
    private Long partId;
    private List<BomMaterialDTO> materials;  // 자재 목록

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BomMaterialDTO {
        private Long materialId;
        private Long quantity;
    }
}
