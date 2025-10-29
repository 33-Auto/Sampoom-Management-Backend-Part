package com.sampoom.backend.api.part.entity;

import com.sampoom.backend.api.part.dto.PartGroupUpdateRequestDTO;
import com.sampoom.backend.common.entity.BaseTimeEntity;
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
    private PartCategory category; // Category 엔티티와 N:1 관계

    @Version
    @Column(nullable = false)
    private Long version; // 버전 필드 추가


    // CSV 로더가 사용할 생성자
    public PartGroup(String code, String name, PartCategory category) {
        this.code = code;
        this.name = name;
        this.category = category;
    }

    // ⭐️ [추가] 수정 메서드 (DTO 사용)
    public void update(PartGroupUpdateRequestDTO requestDTO) {
        // null이 아닐 경우에만 값을 변경 (부분 수정 지원)
        if (requestDTO.getCode() != null) {
            this.code = requestDTO.getCode();
        }
        if (requestDTO.getName() != null) {
            this.name = requestDTO.getName();
        }
    }
}
