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
    private String name; // 선택 입력

    private WorkCenterType type; // 선택 입력

    private WorkCenterStatus status; // 선택 입력

    @DecimalMin(value = "0.0", message = "일일 가동시간은 0 이상이어야 합니다.")
    @DecimalMax(value = "24.0", message = "일일 가동시간은 24 이하여야 합니다.")
    private Integer dailyOperatingHours; // 선택 입력Expand commentComment on lines R23 to R25ResolvedCode has comments. Press enter to view.

    @DecimalMin(value = "0.0",  message = "효율성은 0 이상이어야 합니다.")
    @DecimalMax(value = "100.0",  message = "효율성은 100 이하여야 합니다.")
    private Integer efficiency; // 선택 입력Expand commentComment on lines R27 to R29ResolvedCode has comments. Press enter to view.

    @DecimalMin(value = "0.0", inclusive = false, message = "시간당비용은 0 보다 커야 합니다.")
    private Integer costPerHour; // 선택 입력Expand commentComment on lines R31 to R32ResolvedCode has comments. Press enter to view.
}
