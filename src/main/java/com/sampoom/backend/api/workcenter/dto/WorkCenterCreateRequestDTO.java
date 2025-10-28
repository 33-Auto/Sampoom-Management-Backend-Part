package com.sampoom.backend.api.workcenter.dto;

import com.sampoom.backend.api.workcenter.entity.WorkCenterStatus;
import com.sampoom.backend.api.workcenter.entity.WorkCenterType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkCenterCreateRequestDTO {

    @NotBlank(message = "작업장명을 입력하세요.")
    @Size(max = 120, message = "작업장명은 최대 120자입니다.")
    private String name;

    @NotNull(message = "작업장 유형을 입력하세요.")
    private WorkCenterType type;

    @NotNull(message = "작업장 상태를 입력하세요.")
    private WorkCenterStatus status;

    @NotNull(message = "일일 가동시간을 입력하세요.")
    @DecimalMin(value = "0.0", inclusive = true, message = "일일 가동시간은 0 이상이어야 합니다.")
    @DecimalMax(value = "24.0", inclusive = true, message = "일일 가동시간은 24 이하여야 합니다.")
    private Integer dailyOperatingHours;

    @NotNull(message = "효율성을 입력하세요.")
    @DecimalMin(value = "0.0", inclusive = true, message = "효율성은 0 이상이어야 합니다.")
    @DecimalMax(value = "100.0", inclusive = true, message = "효율성은 100 이하여야 합니다.")
    private Integer efficiency;

    @NotNull(message = "시간당비용을 입력하세요.")
    @DecimalMin(value = "0.0", inclusive = false, message = "시간당비용은 0 보다 커야 합니다.")
    private Integer costPerHour;
}

