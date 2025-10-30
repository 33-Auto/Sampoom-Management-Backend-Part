package com.sampoom.backend.api.material.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialCategoryEvent {
    private String eventId;       // UUID (고유 이벤트 ID)
    private String eventType;     // "MaterialCreated", "MaterialUpdated", "MaterialDeleted"
    private Long version;         // Material 엔티티의 버전 (@Version)
    private String occurredAt;    // ISO-8601 시각 (OffsetDateTime.toString())
    private MaterialCategoryEvent.Payload payload;      // 실제 데이터 (Material 정보)

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {
        private Long categoryId;
        private String name;
        private String code;
        private boolean deleted;
    }
}
