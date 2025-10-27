package com.sampoom.backend.api.domain;

import com.sampoom.backend.common.entitiy.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "part_group")
public class PartGroup extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category; // Category 엔티티와 N:1 관계

    // CSV 로더가 사용할 생성자
    public PartGroup(String code, String name, Category category) {
        this.code = code;
        this.name = name;
        this.category = category;
    }
}
