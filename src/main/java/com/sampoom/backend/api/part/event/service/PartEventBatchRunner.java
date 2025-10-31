package com.sampoom.backend.api.part.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 서버 실행 시 한 번만 실행되는 초기 이벤트 발행 배치 러너
 */
@Component
@RequiredArgsConstructor
public class PartEventBatchRunner implements CommandLineRunner {

    private final PartEventBatchService partEventBatchService;

    @Override
    public void run(String... args) {
        // 필요할 때만 주석 해제해서 실행 (DB 전체 Outbox 등록)
//         partEventBatchService.publishAllPartEvents();
//         partEventBatchService.publishAllPartGroupEvents();
//         partEventBatchService.publishAllPartCategoryEvents();
    }
}
