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

    @ManyToOne(fetch = FetchType.LAZY)
    private MaterialCategory materialCategory;

    /** 이름/단위 수정 */
    public void updateBasicInfo(String name, String unit) {
        this.name = name;
        this.materialUnit = unit;
    }

    /** 카테고리 변경 + 코드 재발급 */
    public void changeCategory(MaterialCategory newCategory, String newCode) {
        this.materialCategory = newCategory;
        this.materialCode = newCode;
    }
}
