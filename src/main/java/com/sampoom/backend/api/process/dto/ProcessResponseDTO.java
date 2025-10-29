package com.sampoom.backend.api.process.dto;

import com.sampoom.backend.api.process.entity.Process;
import com.sampoom.backend.api.process.entity.ProcessStatus;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ProcessResponseDTO {
    private final Long id;
    private final String code;
    private final Long partId;
    private final String partName;
    private final String partCode;
    private final String version;
    private final ProcessStatus status;
    private final Integer stepCount; // 스텝 수 추가
    private final Integer totalSetupMinutes; // 모든 스텝의 준비시간 합계
    private final Integer totalProcessMinutes; // 모든 스텝의 가공시간 합계
    private final Integer totalWaitMinutes; // 모든 스텝의 대기시간 합계
    private final Integer totalStepMinutes; // 모든 스텝의 총 시간 합계
    private final List<ProcessStepResponseDTO> steps;

    public ProcessResponseDTO(Process p) {
        this.id = p.getId();
        this.code = p.getCode();
        this.partId = p.getPart().getId();
        this.partName = p.getPart().getName();
        this.partCode = p.getPart().getCode();
        this.version = p.getVersion();
        this.status = p.getStatus();
        this.stepCount = p.getStepCount(); // 스텝 수 추가
        this.totalSetupMinutes = p.getTotalSetupMinutes();
        this.totalProcessMinutes = p.getTotalProcessMinutes();
        this.totalWaitMinutes = p.getTotalWaitMinutes();
        this.totalStepMinutes = p.getTotalStepMinutes();
        this.steps = p.getSteps().stream().map(ProcessStepResponseDTO::new).collect(Collectors.toList());
    }
}
