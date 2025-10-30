package com.sampoom.backend.api.part.entity;

import com.sampoom.backend.api.part.dto.PartUpdateRequestDTO;
import com.sampoom.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "part_master")
public class Part extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // 부품 코드

    private String name; // 부품명

    private String partUnit;  // 기준단위

    private Integer baseQuantity;  // 기준개수

    private Integer leadTime;  // 리드타임 (단위: 일, Days)

    @Enumerated(EnumType.STRING)
    private PartStatus status;  // 단종

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private PartGroup partGroup; // PartGroup 엔티티와 N:1 관계

    @Version
    @Column(nullable = false)
    private Long version; // 버전 필드 추가

    // CSV 로더가 사용할 생성자
    public Part(String code, String name, PartGroup partGroup, String partUnit, Integer baseQuantity, Integer leadTime) {
        this.code = code;
        this.name = name;
        this.partGroup = partGroup;
        this.partUnit = partUnit;
        this.baseQuantity = baseQuantity;
        this.leadTime = leadTime;
        this.status = PartStatus.ACTIVE;
    }

    // 수정 메서드
    public void update(PartUpdateRequestDTO partUpdateRequestDTO) {
        // 이름이 null이 아닐 경우에만 수정
        if (partUpdateRequestDTO.getName() != null) {
            this.name = partUpdateRequestDTO.getName();
        }

        // 기준단위 수정
        if (partUpdateRequestDTO.getPartUnit() != null) {
            this.partUnit = partUpdateRequestDTO.getPartUnit();
        }

        //  기준개수 수정
        if (partUpdateRequestDTO.getBaseQuantity() != null) {
            this.baseQuantity = partUpdateRequestDTO.getBaseQuantity();
        }

        // 리드타임 수정
        if (partUpdateRequestDTO.getLeadTime() != null) {
            this.leadTime = partUpdateRequestDTO.getLeadTime();
        }
    }

    // 단종 메서드
    public void delete() {
        this.status = PartStatus.DISCONTINUED;
    }

    public void setLeadTime(Integer leadTime) {
        this.leadTime = leadTime;
    }

    public void changeGroup(PartGroup newGroup) {
        this.partGroup = newGroup;
    }

    public void changeCode(String newCode) {
        this.code = newCode;
    }
}
