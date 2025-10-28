package com.sampoom.backend.api.part.repository;

import com.sampoom.backend.api.part.entity.PartCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartCategoryRepository extends JpaRepository<PartCategory, Long> {

    // 코드로 카테고리를 찾기 위한 메서드
    Optional<PartCategory> findByCode(String code);
}
