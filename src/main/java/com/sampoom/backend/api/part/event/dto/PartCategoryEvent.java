package com.sampoom.backend.api.part.event.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartCategoryEvent {

    private String eventId;       // UUID (고유 이벤트 ID)
    private String eventType;     // "CategoryCreated", "CategoryUpdated", "CategoryDeleted"
    private Long version;         // Category 엔티티의 버전
    private String occurredAt;    // ISO-8601 시각 (OffsetDateTime.toString())
    private Payload payload;      // 실제 데이터 (Category 정보)

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Payload {
        private Long categoryId;      // Category ID
        private String categoryName;  // Category Name
        private String categoryCode;  // Category Code
    }
}
