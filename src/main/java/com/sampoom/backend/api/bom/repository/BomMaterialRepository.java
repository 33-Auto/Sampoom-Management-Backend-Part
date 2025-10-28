package com.sampoom.backend.api.bom.repository;

import com.sampoom.backend.api.bom.entity.BomMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BomMaterialRepository extends JpaRepository<BomMaterial,Long> {
}
