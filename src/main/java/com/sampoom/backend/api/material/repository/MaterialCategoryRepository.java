package com.sampoom.backend.api.material.repository;

import com.sampoom.backend.api.material.entity.MaterialCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MaterialCategoryRepository extends JpaRepository<MaterialCategory, Long> {

    Optional<MaterialCategory> findByCode(String code);
}
