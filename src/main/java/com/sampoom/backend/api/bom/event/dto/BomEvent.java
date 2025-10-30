package com.sampoom.backend.api.bom.event.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BomEvent {
    private String eventId;
    private String eventType;
    private String occurredAt;
    private Long version;
    private Payload payload;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Payload {
        private Long bomId;
        private Long partId;
        private String partCode;
        private String partName;
        private String status;
        private String complexity;
        private boolean deleted;
        private List<MaterialInfo> materials;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class MaterialInfo {
            private Long materialId;
            private String materialName;
            private String materialCode;
            private String unit;
            private Long quantity;
        }
    }
}
