package com.sampoom.backend.api.workcenter.dto;

import com.sampoom.backend.api.workcenter.entity.WorkCenterStatus;
import com.sampoom.backend.api.workcenter.entity.WorkCenterType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WorkCenterUpdateRequestDTO {
    private String name;

    private WorkCenterType type;

    private WorkCenterStatus status;

    @DecimalMin(value = "0.0", message = "일일 가동시간은 0 이상이어야 합니다.")
    @DecimalMax(value = "24.0", message = "일일 가동시간은 24 이하여야 합니다.")
    private Integer dailyOperatingHours;

    @DecimalMin(value = "0.0",  message = "효율성은 0 이상이어야 합니다.")
    @DecimalMax(value = "100.0",  message = "효율성은 100 이하여야 합니다.")
    private Integer efficiency;

    @DecimalMin(value = "0.0", inclusive = false, message = "시간당비용은 0 보다 커야 합니다.")
    private Integer costPerHour;
}
