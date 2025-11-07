package com.sampoom.backend.api.part.event.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartEvent {

    private String eventId;       // UUID (고유 이벤트 ID)
    private String eventType;     // "PartCreated", "PartUpdated", "PartDeleted"
    private Long version;         // Part 엔티티의 버전 (@Version)
    private String occurredAt;    // ISO-8601 시각 (OffsetDateTime.toString())
    private Payload payload;      // 실제 데이터 (Part 정보)

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Payload {
        private Long partId;
        private String code;
        private String name;

        private String partUnit;
        private Integer baseQuantity;  // 안전재고
        private Integer leadTime;
        private Integer standardQuantity;  // 기준수량

        private String status;
        private Boolean deleted;

        private Long groupId;
        private Long categoryId;

        private Long standardCost;
        private Long standardTotalCost; // 표준 총비용 추가
    }
}
