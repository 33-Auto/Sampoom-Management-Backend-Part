package com.sampoom.backend.api.workcenter.entity;

import com.sampoom.backend.common.entitiy.BaseTimeEntity;
import com.sampoom.backend.common.entitiy.SoftDeleteEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "work_center", uniqueConstraints = {
        @UniqueConstraint(name = "uk_work_center_name", columnNames = "name")
})
@SQLDelete(sql = "UPDATE work_center " +
        "SET deleted = true, deleted_at = now() " +
        "WHERE work_center_id = ?")
@SQLRestriction("deleted = false")
@Builder
public class WorkCenter extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "work_center_id")
    private Long id;

    @Column(nullable = false, length = 120, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkCenterType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkCenterStatus status;

    // 일일 가동시간(시간), 소수 2자리
    @Column(nullable = false)
    private Integer dailyOperatingHours;

    // 효율성(0~100), 소수 2자리
    @Column(nullable = false)
    private Integer efficiency;

    // 시간당 비용, 양수
    @Column(nullable = false, precision = 12, scale = 2)
    private Integer costPerHour;

    public void changeName(String name) { this.name = name; }
    public void changeType(WorkCenterType type) { this.type = type; }
    public void changeStatus(WorkCenterStatus status) { this.status = status; }
    public void changeDailyOperatingHours(Integer hours) { this.dailyOperatingHours = hours; }
    public void changeEfficiency(Integer efficiency) { this.efficiency = efficiency; }
    public void changeCostPerHour(Integer costPerHour) { this.costPerHour = costPerHour; }
}
