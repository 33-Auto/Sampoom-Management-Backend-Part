package com.sampoom.backend.api.material.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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

    @Column(name = "lead_time")
    private Integer leadTime; // 리드타임

    @ManyToOne(fetch = FetchType.LAZY)
    private MaterialCategory materialCategory;


    @Column(precision = 15)
    private Long standardCost; // 표준단가

    @Version
    private Long version; // JPA가 자동 관리 (낙관적 락 + 자동 증가)

    /** 이름/단위/기준단위/리드타임 수정 */
    public void updateBasicInfo(String name, String unit, Integer baseQuantity, Integer leadTime, Long standardCost) {
        this.name = name;
        this.materialUnit = unit;
        this.baseQuantity = baseQuantity;
        this.leadTime = leadTime;
        this.standardCost = standardCost;
    }

    /** 카테고리 변경 + 코드 재발급 */
    public void changeCategory(MaterialCategory newCategory, String newCode) {
        this.materialCategory = newCategory;
        this.materialCode = newCode;
    }
}
