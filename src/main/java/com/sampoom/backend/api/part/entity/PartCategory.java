package com.sampoom.backend.api.part.entity;

import com.sampoom.backend.api.part.dto.PartCategoryUpdateRequestDTO;
import com.sampoom.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "part_category")
public class PartCategory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String code;

    @Version
    @Column(nullable = false)
    private Long version; // 버전 필드 추가


    // CSV 로더가 사용할 생성자
    public PartCategory(String code, String name) {
        this.code = code;
        this.name = name;
    }

    //  수정 메서드 (DTO 사용)
    public void update(PartCategoryUpdateRequestDTO requestDTO) {
        // null이 아닐 경우에만 값을 변경
        if (requestDTO.getCode() != null) {
            this.code = requestDTO.getCode();
        }
        if (requestDTO.getName() != null) {
            this.name = requestDTO.getName();
        }
    }
}
