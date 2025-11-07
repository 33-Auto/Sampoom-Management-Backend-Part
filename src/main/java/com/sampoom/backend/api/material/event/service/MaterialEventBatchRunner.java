package com.sampoom.backend.api.material.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaterialEventBatchRunner implements CommandLineRunner {

    private final MaterialEventBatchService materialEventBatchService;

    @Override
    public void run(String... args) {
        try {
//            materialEventBatchService.publishAllMaterialCategoryEvents();
            materialEventBatchService.publishAllMaterialEvents();

            log.info("✅ 모든 Material 관련 이벤트 발행 완료");
        } catch (Exception e) {
            log.error("❌ Material 이벤트 배치 실행 중 오류 발생", e);
        }
    }
}