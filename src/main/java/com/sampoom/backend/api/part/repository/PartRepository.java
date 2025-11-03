package com.sampoom.backend.api.part.repository;

import com.sampoom.backend.api.part.entity.PartStatus;
import com.sampoom.backend.api.part.entity.Part;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface PartRepository extends JpaRepository<Part, Long>, JpaSpecificationExecutor<Part> {

    // 카테고리별 부품 조회
    Page<Part> findByPartGroupCategoryIdAndStatus(Long categoryId, PartStatus status, Pageable pageable);

    // 이름 or 코드로 검색 + ACTIVE 상태만
    Page<Part> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCaseAndStatus(
            String nameKeyword,
            String codeKeyword,
            PartStatus status,
            Pageable pageable
    );

    @Query("""
select p from Part p
where p.status = :status
  and (lower(p.name) like lower(concat('%', :kw, '%'))
       or lower(p.code) like lower(concat('%', :kw, '%')))
""")
    Page<Part> searchActive(@Param("kw") String kw, @Param("status") PartStatus status, Pageable pageable);

    // 그룹별 부품 조회
    Page<Part> findByPartGroupId(Long groupId, Pageable pageable);

    // 가장 최근 부품 (코드 자동 생성용)
    Part findTopByPartGroupIdOrderByIdDesc(Long groupId);

    Part findTopByPartGroupIdOrderByCodeDesc(Long groupId);

    // N+1 방지 (Part -> PartGroup -> PartCategory)
    @Query("SELECT p FROM Part p JOIN FETCH p.partGroup g JOIN FETCH g.category c")
    List<Part> findAllForBootstrap();

    @Query("SELECT p FROM Part p " +
            "LEFT JOIN FETCH p.partGroup g " +
            "LEFT JOIN FETCH g.category c")
    List<Part> findAllWithGroupAndCategory();

    // 상태별 Part 조회 (일괄 업데이트용)
    List<Part> findByStatus(PartStatus status);
}
