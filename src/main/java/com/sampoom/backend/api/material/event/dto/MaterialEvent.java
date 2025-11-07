package com.sampoom.backend.api.material.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialEvent {

    private String eventId;       // UUID (고유 이벤트 ID)
    private String eventType;     // "MaterialCreated", "MaterialUpdated", "MaterialDeleted"
    private Long version;         // Material 엔티티의 버전 (@Version)
    private String occurredAt;    // ISO-8601 시각 (OffsetDateTime.toString())
    private MaterialEvent.Payload payload;      // 실제 데이터 (Material 정보)

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {
        private Long materialId;
        private String materialCode;
        private String name;
        private String materialUnit;
        private Integer baseQuantity;
        private Integer standardQuantity;  // 기준 수량 추가
        private Integer leadTime;
        private boolean deleted;
        private Long materialCategoryId;
        private Long standardCost;
        private Long standardTotalCost; // 표준총비용 추가
    }
}
