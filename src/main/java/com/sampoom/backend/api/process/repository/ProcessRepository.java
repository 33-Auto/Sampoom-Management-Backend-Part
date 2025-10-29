package com.sampoom.backend.api.process.repository;

import com.sampoom.backend.api.process.entity.Process;
import com.sampoom.backend.api.process.entity.ProcessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProcessRepository extends JpaRepository<Process, Long> {
    // 코드 자동 생성을 위한 메서드 추가
    @Query("SELECT p.code FROM Process p WHERE p.code LIKE 'PC-%' ORDER BY p.code DESC LIMIT 1")
    String findLastProcessCode();

    boolean existsByCode(String code);

    // 검색 메서드 추가 (공정코드, 부품명으로 검색, 상태 필터)
    @Query("""
        SELECT p FROM Process p 
        JOIN FETCH p.part part
        LEFT JOIN FETCH p.steps steps
        LEFT JOIN FETCH steps.workCenter wc
        WHERE (:q IS NULL OR :q = '' 
               OR LOWER(p.code) LIKE CONCAT('%', LOWER(:q), '%')
               OR LOWER(part.name) LIKE CONCAT('%', LOWER(:q), '%'))
        AND (:status IS NULL OR p.status = :status)
        """)
    Page<Process> search(
            @Param("q") String q,
            @Param("status") ProcessStatus status,
            Pageable pageable
    );
}
