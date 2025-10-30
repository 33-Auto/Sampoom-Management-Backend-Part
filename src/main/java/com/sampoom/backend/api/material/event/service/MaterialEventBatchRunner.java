package com.sampoom.backend.api.material.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaterialEventBatchRunner implements CommandLineRunner {

    private final MaterialEventBatchService materialEventBatchService;

    @Override
    public void run(String... args) {
    }
}