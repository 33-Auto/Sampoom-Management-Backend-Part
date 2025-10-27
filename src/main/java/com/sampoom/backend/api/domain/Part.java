package com.sampoom.backend.api.domain;

import com.sampoom.backend.api.dto.PartCreateRequestDTO;
import com.sampoom.backend.api.dto.PartUpdateRequestDTO;
import com.sampoom.backend.common.entitiy.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "part")
public class Part extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // 부품 코드

    private String name; // 부품명


    @Enumerated(EnumType.STRING)
    private PartStatus status;  // 단종

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private PartGroup partGroup; // PartGroup 엔티티와 N:1 관계

    // CSV 로더가 사용할 생성자
    public Part(String code, String name, PartGroup partGroup) {
        this.code = code;
        this.name = name;
        this.partGroup = partGroup;
        this.status = PartStatus.ACTIVE;
    }

    // 생성 메서드
    public static Part create(PartCreateRequestDTO partCreateRequestDTO, PartGroup partGroup) {
        Part part = new Part();
        part.code = partCreateRequestDTO.getCode();
        part.name = partCreateRequestDTO.getName();
        part.partGroup = partGroup; // 연관관계 설정
        part.status = PartStatus.ACTIVE;

        return part;
    }

    // 수정 메서드
    public void update(PartUpdateRequestDTO partUpdateRequestDTO) {
        // 이름이 null이 아닐 경우에만 수정
        if (partUpdateRequestDTO.getName() != null) {
            this.name = partUpdateRequestDTO.getName();
        }
    }

    // 단종 메서드
    public void delete() {
        this.status = PartStatus.DISCONTINUED;
    }
}
