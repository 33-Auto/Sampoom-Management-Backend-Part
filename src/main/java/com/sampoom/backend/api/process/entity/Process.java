package com.sampoom.backend.api.process.entity;

import com.sampoom.backend.api.part.entity.Part;
import com.sampoom.backend.api.workcenter.entity.WorkCenter;
import com.sampoom.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "process_master")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Process extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20, unique = true)
    private String code; // PC-001 형태

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    @Column(nullable = false, length = 50)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProcessStatus status;

    @Column(nullable = false)
    @Builder.Default
    private Integer stepCount = 0; // 스텝 수

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1; // 생산량

    // 모든 스텝의 총 시간 합계 컬럼들 추가
    @Column(nullable = false)
    @Builder.Default
    private Integer totalSetupMinutes = 0; // 모든 스텝의 준비시간 합계

    @Column(nullable = false)
    @Builder.Default
    private Integer totalProcessMinutes = 0; // 모든 스텝의 가공시간 합계

    @Column(nullable = false)
    @Builder.Default
    private Integer totalWaitMinutes = 0; // 모든 스텝의 대기시간 합계

    @Column(nullable = false)
    @Builder.Default
    private Integer totalStepMinutes = 0; // 모든 스텝의 총 시간 합계

    // 총 공정비용 컬럼 추가
    @Column(nullable = false)
    @Builder.Default
    private Long totalProcessCost = 0L; // 총 공정비용 (원)

    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    @Builder.Default
    private List<ProcessStep> steps = new ArrayList<>();

    public void addStep(ProcessStep step) {
        steps.add(step);
        step.setProcess(this);
        updateStepCount();
        updateTotalTimes();
    }

    public void clearSteps() {
        steps.clear();
        updateStepCount();
        updateTotalTimes();
    }

    private void updateStepCount() {
        this.stepCount = steps.size();
    }

    // 모든 스텝의 시간을 합계하여 총 시간 업데이트
    private void updateTotalTimes() {
        this.totalSetupMinutes = steps.stream()
                .mapToInt(step -> step.getSetupMinutes() != null ? step.getSetupMinutes() : 0)
                .sum();

        this.totalProcessMinutes = steps.stream()
                .mapToInt(step -> step.getProcessMinutes() != null ? step.getProcessMinutes() : 0)
                .sum();

        this.totalWaitMinutes = steps.stream()
                .mapToInt(step -> step.getWaitMinutes() != null ? step.getWaitMinutes() : 0)
                .sum();

        this.totalStepMinutes = this.totalSetupMinutes + this.totalProcessMinutes + this.totalWaitMinutes;

        // 시간이 업데이트되면 공정비용도 함께 계산
        updateTotalProcessCost();
    }

    // 총 공정비용을 계산하는 메서드
    private void updateTotalProcessCost() {
        this.totalProcessCost = 0L;

        for (ProcessStep step : steps) {
            if (step.getWorkCenter() != null) {
                WorkCenter workCenter = step.getWorkCenter();
                long hourlyRate = workCenter.getCostPerHour();
                int dailyOperatingHours = workCenter.getDailyOperatingHours();
                int efficiency = workCenter.getEfficiency();

                // 각 스텝의 총 시간(분)을 계산
                int stepTotalMinutes = (step.getSetupMinutes() != null ? step.getSetupMinutes() : 0) +
                                     (step.getProcessMinutes() != null ? step.getProcessMinutes() : 0) +
                                     (step.getWaitMinutes() != null ? step.getWaitMinutes() : 0);

                // dailyOperatingHours를 활용한 실제 가동률 반영
                // 실제 시간당 비용 = 기본 시간당 비용 × (24시간 ÷ 일일 운영시간) × (100 ÷ 효율성)
                double operatingFactor = 24.0 / dailyOperatingHours; // 가동률 반영
                double efficiencyFactor = 100.0 / efficiency; // 효율성 반영
                double adjustedHourlyRate = hourlyRate * operatingFactor * efficiencyFactor;

                // 분을 시간으로 변환하고 조정된 시간당 비용을 곱해서 스텝 비용 계산
                long stepCost = Math.round((stepTotalMinutes / 60.0) * adjustedHourlyRate);
                this.totalProcessCost += stepCost;
            }
        }
    }

    // 공정비용 수동 재계산 메서드 (외부에서 호출 가능)
    public void recalculateProcessCost() {
        updateTotalProcessCost();
    }

    public void changeCode(String code) { this.code = code; }
    public void changeVersion(String version) { this.version = version; }
    public void changeStatus(ProcessStatus status) { this.status = status; }
    public void changePart(Part part) { this.part = part; }
    public void changeQuantity(Integer quantity) { this.quantity = quantity; }
}
