package com.sampoom.backend.api.workcenter.entity;

import com.sampoom.backend.common.entity.SoftDeleteEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

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


    @Column(nullable = false)
    private Integer dailyOperatingHours;


    @Column(nullable = false)
    private Integer efficiency;


    @Column(nullable = false)
    private Integer costPerHour;

    public void changeName(String name) { this.name = name; }
    public void changeType(WorkCenterType type) { this.type = type; }
    public void changeStatus(WorkCenterStatus status) { this.status = status; }
    public void changeDailyOperatingHours(Integer hours) { this.dailyOperatingHours = hours; }
    public void changeEfficiency(Integer efficiency) { this.efficiency = efficiency; }
    public void changeCostPerHour(Integer costPerHour) { this.costPerHour = costPerHour; }
}
