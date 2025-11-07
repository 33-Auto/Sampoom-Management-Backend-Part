package com.sampoom.backend.api.material.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "material_master")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String materialCode;

    private String materialUnit;

    @Column(name = "base_quantity")
    private Integer baseQuantity; // 기준단위 (몇개씩 넣을지)

    @Column(name = "standard_quantity", nullable = false, columnDefinition = "INTEGER DEFAULT 1")
    @Builder.Default
    private Integer standardQuantity = 1; // 기준 수량 (기본값 1)

    @Column(name = "lead_time")
    private Integer leadTime; // 리드타임

    @ManyToOne(fetch = FetchType.LAZY)
    private MaterialCategory materialCategory;

    private Long standardCost; // 표준단가

    @Column(name = "standard_total_cost")
    private Long standardTotalCost; // 표준총비용 (standardCost * standardQuantity)

    @Version
    private Long version; // JPA가 자동 관리 (낙관적 락 + 자동 증가)

    @PostLoad
    @PrePersist
    @PreUpdate
    private void updateStandardTotalCost() {
        this.standardTotalCost = calculateStandardTotalCost();
    }

    /** 이름/단위/기준단위/리드타임/기준수량 수정 */
    public void updateBasicInfo(String name, String unit, Integer baseQuantity, Integer standardQuantity, Integer leadTime, Long standardCost) {
        this.name = name;
        this.materialUnit = unit;
        this.baseQuantity = baseQuantity;
        this.standardQuantity = standardQuantity != null ? standardQuantity : 1;
        this.leadTime = leadTime;
        this.standardCost = standardCost;
        updateStandardTotalCost();
    }

    /** 카테고리 변경 + 코드 재발급 */
    public void changeCategory(MaterialCategory newCategory, String newCode) {
        this.materialCategory = newCategory;
        this.materialCode = newCode;
    }

    /** 표준총비용 계산 */
    private Long calculateStandardTotalCost() {
        if (this.standardCost == null || this.standardQuantity == null) {
            return null;
        }
        return this.standardCost * this.standardQuantity;
    }

    /** 표준비용 업데이트 시 총비용도 함께 업데이트 */
    public void updateStandardCost(Long standardCost) {
        this.standardCost = standardCost;
        updateStandardTotalCost();
    }

    /** 표준수량 업데이트 시 총비용도 함께 업데이트 */
    public void updateStandardQuantity(Integer standardQuantity) {
        this.standardQuantity = standardQuantity != null ? standardQuantity : 1;
        updateStandardTotalCost();
    }
}
