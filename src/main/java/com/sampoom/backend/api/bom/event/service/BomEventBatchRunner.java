package com.sampoom.backend.api.bom.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BomEventBatchRunner implements CommandLineRunner {

    private final BomEventBatchService bomEventBatchService;

    @Override
    public void run(String... args) {
        // 애플리케이션 실행 시 한 번만 돌림
//        bomEventBatchService.publishAllBomEvents();
    }
}
