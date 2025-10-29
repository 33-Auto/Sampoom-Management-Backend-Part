package com.sampoom.backend.api.part.repository;

import com.sampoom.backend.api.part.entity.PartCategory;
import com.sampoom.backend.api.part.entity.PartGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PartGroupRepository extends JpaRepository<PartGroup, Long> {

    // 카테고리 id로 모든 그룹 찾는 메서드
    List<PartGroup> findByCategoryId(Long categoryId);

    // 카테고리와 코드로 그룹을 찾기 위한 메서드
    Optional<PartGroup> findByCodeAndCategory(String code, PartCategory category);

    // N+1 방지 (PartGroup -> PartCategory)
    @Query("SELECT g FROM PartGroup g JOIN FETCH g.category c")
    List<PartGroup> findAllForBootstrap();

    // 특정 categoryId를 가진 그룹이 존재하는지 확인하는 메서드
    boolean existsByCategoryId(Long categoryId);
}
