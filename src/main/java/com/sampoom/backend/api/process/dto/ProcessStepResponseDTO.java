package com.sampoom.backend.api.process.dto;

import com.sampoom.backend.api.process.entity.ProcessStep;
import lombok.Getter;

@Getter
public class ProcessStepResponseDTO {
    private final Integer stepOrder;
    private final String stepName;
    private final Long workCenterId;
    private final String workCenterCode;
    private final String workCenterName;
    private final Integer setupMinutes;
    private final Integer processMinutes;
    private final Integer waitMinutes;
    private final Integer totalMinutes;

    public ProcessStepResponseDTO(ProcessStep s) {

        this.stepOrder = s.getStepOrder();
        this.stepName = s.getStepName();
        this.workCenterId = s.getWorkCenter().getId();
        this.workCenterCode = s.getWorkCenter().getCode();
        this.workCenterName = s.getWorkCenter().getName();
        this.setupMinutes = s.getSetupMinutes();
        this.processMinutes = s.getProcessMinutes();
        this.waitMinutes = s.getWaitMinutes();
        this.totalMinutes = s.getTotalMinutes();
    }
}
