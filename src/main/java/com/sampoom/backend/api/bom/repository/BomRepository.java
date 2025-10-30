package com.sampoom.backend.api.bom.repository;

import com.sampoom.backend.api.bom.entity.Bom;
import com.sampoom.backend.api.bom.entity.BomComplexity;
import com.sampoom.backend.api.bom.entity.BomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BomRepository extends JpaRepository<Bom, Long> {

    @Query("""
SELECT b FROM Bom b
JOIN b.part p
JOIN p.partGroup g
JOIN g.category c
WHERE (
  COALESCE(:keyword, '') = ''  
  OR p.name ILIKE CONCAT('%', :keyword, '%')
  OR p.code ILIKE CONCAT('%', :keyword, '%')
)
AND (:categoryId IS NULL OR c.id = :categoryId)
AND (:groupId    IS NULL OR g.id = :groupId)
AND (:status IS NULL OR b.status = :status)
AND (:complexity IS NULL OR b.complexity = :complexity)
ORDER BY b.createdAt DESC
""")
    Page<Bom> findByFilters(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("groupId") Long groupId,
            @Param("status") BomStatus status,
            @Param("complexity") BomComplexity complexity,
            Pageable pageable);

    Optional<Bom> findByPart_Id(Long partId);
}