package com.sampoom.backend.api.bom.entity;

import com.sampoom.backend.api.part.entity.Part;
import com.sampoom.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bom")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Bom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bom_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", unique = true)
    private Part part;

    @OneToMany(mappedBy = "bom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BomMaterial> materials = new ArrayList<>();

    /** BOM 상태 (활성/검토중/비활성/승인대기) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BomStatus status = BomStatus.PENDING_APPROVAL;

    /** BOM 복잡도 (단순/보통/복잡) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BomComplexity complexity = BomComplexity.SIMPLE;

    @Column(name = "total_cost", nullable = false)
    @Builder.Default
    private Long totalCost = 0L;

    @Column(name = "bom_code", unique = true, nullable = false, length = 30)
    private String bomCode;

    @Version
    private Long version;

    public void addMaterial(BomMaterial bomMaterial) {
        this.materials.add(bomMaterial);

        if (bomMaterial.getBom() != this) {
            bomMaterial.updateBom(this);
        }

        // 복잡도 재계산
        calculateComplexity();

        // 자재 추가 시 총비용 재계산
        calculateTotalCost();
    }

    /** 총비용 계산 */
    public void calculateTotalCost() {
        this.totalCost = this.materials.stream()
                .mapToLong(m -> {
                    if (m.getMaterial().getStandardCost() == null) return 0L;
                    return m.getMaterial().getStandardCost() * m.getQuantity();
                })
                .sum();
    }

    // 수정일 갱신
    public void touchNow() { this.updatedAt = LocalDateTime.now(); }

    /** 복잡도 자동 계산 로직 */
    public void calculateComplexity() {
        int count = this.materials.size();
        if (count <= 2) this.complexity = BomComplexity.SIMPLE;
        else if (count <= 4) this.complexity = BomComplexity.NORMAL;
        else this.complexity = BomComplexity.COMPLEX;
    }

    /** 상태 변경 */
    public void updateStatus(BomStatus newStatus) {
        this.status = newStatus;
        touchNow();
    }

    // 코드 생성
    public void generateBomCode(Long id) {
        this.bomCode = String.format("BOM-%03d", id);
    }
}
