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

    public void addMaterial(BomMaterial bomMaterial) {
        this.materials.add(bomMaterial);

        if (bomMaterial.getBom() != this) {
            bomMaterial.updateBom(this);
        }
    }

    public void touchNow() { this.updatedAt = LocalDateTime.now(); }
}
