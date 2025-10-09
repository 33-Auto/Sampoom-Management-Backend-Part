package com.sampoom.backend.api.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartGroupRepository extends JpaRepository<PartGroup, Long> {

    // 카테고리 id로 모든 그룹 찾는 메서드
    List<PartGroup> findByCategoryId(Long categoryId);
}
