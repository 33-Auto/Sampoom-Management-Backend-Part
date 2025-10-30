package com.sampoom.backend.api.material.repository;

import com.sampoom.backend.api.material.entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaterialRepository extends JpaRepository<Material,Long>, JpaSpecificationExecutor<Material> {

    // 카테고리별 자재 조회 (페이지네이션)
    Page<Material> findByMaterialCategoryId(Long categoryId, Pageable pageable);

    // 이름 또는 코드 검색 (부분 일치, 대소문자 무시)
    Page<Material> findByNameContainingIgnoreCaseOrMaterialCodeContainingIgnoreCase(String name, String code, Pageable pageable);

    @Query("""
    select m from Material m
    where (:kw is null or :kw = '' 
           or lower(m.name) like lower(concat('%', :kw, '%'))
           or lower(m.materialCode) like lower(concat('%', :kw, '%')))
    """)
    Page<Material> search(@Param("kw") String keyword, Pageable pageable);


    // 카테고리 내에서 가장 최근 등록된 자재 찾기 (코드 자동 생성용)
    Material findTopByMaterialCategoryIdOrderByIdDesc(Long categoryId);
}