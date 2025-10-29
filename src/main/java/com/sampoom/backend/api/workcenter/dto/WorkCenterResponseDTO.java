package com.sampoom.backend.api.workcenter.dto;

import com.sampoom.backend.api.workcenter.entity.WorkCenter;
import com.sampoom.backend.api.workcenter.entity.WorkCenterStatus;
import com.sampoom.backend.api.workcenter.entity.WorkCenterType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkCenterResponseDTO {
    private Long id;
    private String name;
    private WorkCenterType type;
    private WorkCenterStatus status;
    private Integer dailyOperatingHours;
    private Integer efficiency;
    private Integer costPerHour;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static WorkCenterResponseDTO from(WorkCenter wc) {
        return WorkCenterResponseDTO.builder()
                .id(wc.getId())
                .name(wc.getName())
                .type(wc.getType())
                .status(wc.getStatus())
                .dailyOperatingHours(wc.getDailyOperatingHours())
                .efficiency(wc.getEfficiency())
                .costPerHour(wc.getCostPerHour())
                .createdAt(wc.getCreatedAt())
                .updatedAt(wc.getUpdatedAt())
                .build();
    }
}
