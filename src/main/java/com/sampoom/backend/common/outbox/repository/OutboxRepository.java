package com.sampoom.backend.common.outbox.repository;

import com.sampoom.backend.common.outbox.entity.Outbox;
import com.sampoom.backend.common.outbox.entity.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutboxRepository extends JpaRepository<Outbox, Long> {

    // READY와 FAILED 상태를 모두 조회하되, 재시도 횟수가 maxRetryCount 미만인 것만 조회
    @Query("SELECT o FROM Outbox o WHERE (o.status = 'READY' OR (o.status = 'FAILED' AND o.retryCount < :maxRetryCount)) ORDER BY o.createdAt ASC")
    List<Outbox> findTop10ByStatusReadyOrFailedWithRetryLimitOrderByCreatedAtAsc(@Param("maxRetryCount") int maxRetryCount);
}
