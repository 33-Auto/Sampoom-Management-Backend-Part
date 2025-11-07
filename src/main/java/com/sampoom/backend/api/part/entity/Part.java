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

    private Integer baseQuantity;  // 안전재고

    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 1")
    private Integer standardQuantity = 1;  // 기준 수량 (기본값 1)

    private Integer leadTime = 0;  // 리드타임 (공정 기준 자동으로, 기본값 0)

    @Enumerated(EnumType.STRING)
    private PartStatus status;  // 단종

    private Long standardCost; // 표준 단가 (자동 계산, 입력 X)

    @Column(name = "standard_total_cost")
    private Long standardTotalCost; // 표준 총비용 (BOM 비용 * 기준수량 + 공정비)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private PartGroup partGroup; // PartGroup 엔티티와 N:1 관계

    @Version
    @Column(nullable = false)
    private Long version; // 버전 필드 추가

    public Part(String code, String name, PartGroup partGroup, String partUnit, Integer baseQuantity, Integer standardQuantity) {
        this.code = code;
        this.name = name;
        this.partGroup = partGroup;
        this.partUnit = partUnit;
        this.baseQuantity = baseQuantity;
        this.standardQuantity = standardQuantity;
        this.leadTime = 0;
        this.status = PartStatus.ACTIVE;
        this.standardCost = 0L;
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

        //  기준 수량 수정
        if (partUpdateRequestDTO.getStandardQuantity() != null) {
            this.standardQuantity = partUpdateRequestDTO.getStandardQuantity();
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

    // Part의 총 비용을 BOM 비용과 Process 비용을 합쳐서 계산하는 메서드
    public void calculateStandardCost(Long bomCost, Long processCost) {
        // standard_total_cost = bom 비용 * standard_quantity + 공정비
        Long bomTotalCost = (bomCost != null ? bomCost : 0L) * (standardQuantity != null ? standardQuantity : 1);
        Long totalProcessCost = processCost != null ? processCost : 0L;
        this.standardTotalCost = bomTotalCost + totalProcessCost;

        // standard_cost = standard_total_cost / standard_quantity (1000원 단위로 반올림)
        Long rawStandardCost = this.standardQuantity != null && this.standardQuantity > 0
            ? this.standardTotalCost / this.standardQuantity
            : this.standardTotalCost;
        this.standardCost = roundToThousand(rawStandardCost);
    }

    // BOM 비용과 공정비를 분리해서 계산하는 새로운 메서드
    public void calculateStandardCostAndTotal(Long bomCost, Long processCost) {
        Long bomUnitCost = bomCost != null ? bomCost : 0L;
        Long processUnitCost = processCost != null ? processCost : 0L;
        Integer qty = standardQuantity != null ? standardQuantity : 1;

        // standard_total_cost = bom 비용 * standard_quantity + 공정비
        this.standardTotalCost = (bomUnitCost * qty) + processUnitCost;

        // standard_cost = standard_total_cost / standard_quantity (1000원 단위로 반올림)
        Long rawStandardCost = qty > 0 ? this.standardTotalCost / qty : this.standardTotalCost;
        this.standardCost = roundToThousand(rawStandardCost);
    }

    // standardCost 직접 설정 메서드 (필요시)
    public void setStandardCost(Long standardCost) {
        this.standardCost = roundToThousand(standardCost);
    }

    // standardTotalCost 직접 설정 메서드
    public void setStandardTotalCost(Long standardTotalCost) {
        this.standardTotalCost = standardTotalCost;
        // standard_cost 재계산 (1000원 단위로 반올림)
        Long rawStandardCost = this.standardQuantity != null && this.standardQuantity > 0
            ? this.standardTotalCost / this.standardQuantity
            : this.standardTotalCost;
        this.standardCost = roundToThousand(rawStandardCost);
    }

    // standardQuantity 변경 시 비용 재계산
    public void updateStandardQuantity(Integer standardQuantity) {
        this.standardQuantity = standardQuantity;
        // 기존 standardTotalCost가 있다면 standardCost 재계산 (1000원 단위로 반올림)
        if (this.standardTotalCost != null) {
            Long rawStandardCost = standardQuantity != null && standardQuantity > 0
                ? this.standardTotalCost / standardQuantity
                : this.standardTotalCost;
            this.standardCost = roundToThousand(rawStandardCost);
        }
    }

    /**
     * 1000원 단위로 반올림하는 헬퍼 메서드
     */
    private Long roundToThousand(Long amount) {
        if (amount == null) {
            return null;
        }
        // 1000으로 나누고 반올림한 후 다시 1000을 곱함
        return Math.round(amount / 1000.0) * 1000L;
    }
}
