package com.sampoom.backend.api.workcenter.repository;

import com.sampoom.backend.api.workcenter.entity.WorkCenter;
import com.sampoom.backend.api.workcenter.entity.WorkCenterStatus;
import com.sampoom.backend.api.workcenter.entity.WorkCenterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkCenterRepository extends JpaRepository<WorkCenter, Long>, JpaSpecificationExecutor<WorkCenter> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);



    @Query(value = "SELECT code FROM work_center WHERE code LIKE 'WC-%' ORDER BY code DESC LIMIT 1 FOR UPDATE", nativeQuery = true)
    String findLastWorkCenterCodeWithLock();

    boolean existsByCode(String code);

    @Query(
            "SELECT w FROM WorkCenter w " +
                    "WHERE (w.deleted = false) " +
                    "AND (:type IS NULL OR w.type = :type) " +
                    "AND (:status IS NULL OR w.status = :status) " +
                    "AND ( :q IS NULL OR :q = '' " +
                    "      OR LOWER(w.name) LIKE CONCAT('%', LOWER(:q), '%') " +
                    "    )"
    )
    Page<WorkCenter> search(
            @Param("q") String q,
            @Param("type") WorkCenterType type,
            @Param("status") WorkCenterStatus status,
            Pageable pageable
    );
}
