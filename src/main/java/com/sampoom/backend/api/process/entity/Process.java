package com.sampoom.backend.api.process.entity;

import com.sampoom.backend.api.part.entity.Part;
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
    }

    public void changeCode(String code) { this.code = code; }
    public void changeVersion(String version) { this.version = version; }
    public void changeStatus(ProcessStatus status) { this.status = status; }
    public void changePart(Part part) { this.part = part; }
    public void changeQuantity(Integer quantity) { this.quantity = quantity; }
}
