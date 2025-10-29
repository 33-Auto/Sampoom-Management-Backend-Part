package com.sampoom.backend.api.process.entity;

import com.sampoom.backend.api.workcenter.entity.WorkCenter;
import com.sampoom.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "process_step")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProcessStep extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "process_id", nullable = false)
    @Setter
    private Process process;

    @Column(nullable = false)
    private Integer stepOrder; // 1부터 시작 권장

    @Column(nullable = false, length = 120)
    private String stepName;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_center_id", nullable = false)
    private WorkCenter workCenter;

    @Column(nullable = false)
    private Integer setupMinutes;   // 준비시간(분)

    @Column(nullable = false)
    private Integer processMinutes; // 가공시간(분)

    @Column(nullable = false)
    private Integer waitMinutes;    // 대기시간(분)

    @Column(nullable = false)
    private Integer totalMinutes = 0;   // 총 시간(분) = 위 3개 합

    public void computeTotal() {
        int s = setupMinutes == null ? 0 : setupMinutes;
        int p = processMinutes == null ? 0 : processMinutes;
        int w = waitMinutes == null ? 0 : waitMinutes;
        this.totalMinutes = s + p + w;
    }

    @PrePersist
    @PreUpdate
    private void onPersistOrUpdate() {
                computeTotal();
    }
}

