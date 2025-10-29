package com.sampoom.backend.api.part.event.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartGroupEvent {

    private String eventId;       // UUID (고유 이벤트 ID)
    private String eventType;     // "PartGroupCreated", "PartGroupUpdated", "PartGroupDeleted"
    private Long version;         // PartGroup 엔티티의 버전
    private String occurredAt;    // ISO-8601 시각 (OffsetDateTime.toString())
    private Payload payload;      // 실제 데이터 (PartGroup 정보)

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Payload {
        private Long groupId;
        private String groupName;
        private String groupCode;
        private Long categoryId;
    }
}
