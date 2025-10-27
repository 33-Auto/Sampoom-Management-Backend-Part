package com.sampoom.backend.api.part.repository;

import com.sampoom.backend.api.part.entity.PartStatus;
import com.sampoom.backend.api.part.entity.Part;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PartRepository extends JpaRepository<Part, Long> {

    // 그룹 ID로 모든 부품을 찾는 메서드
    List<Part> findByPartGroupId(Long groupId);

    List<Part> findByPartGroupIdAndStatus(Long groupId, PartStatus status);

    @Query("SELECT p FROM Part p WHERE (p.code LIKE %:keyword% OR p.name LIKE %:keyword%) AND p.status = 'ACTIVE'")
    List<Part> searchByKeyword(@Param("keyword") String keyword);
}
