package com.sampoom.backend.common.config;

import com.opencsv.CSVReader;
import com.sampoom.backend.api.part.entity.PartCategory;
import com.sampoom.backend.api.part.entity.Part;
import com.sampoom.backend.api.part.entity.PartGroup;
import com.sampoom.backend.api.part.repository.PartCategoryRepository;
import com.sampoom.backend.api.part.repository.PartGroupRepository;
import com.sampoom.backend.api.part.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class CsvDataLoader implements CommandLineRunner {

    private final PartCategoryRepository categoryRepository;
    private final PartGroupRepository partGroupRepository;
    private final PartRepository partRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (partRepository.count() > 0) {  // 데이터 이미 존재하면 건너뜀
            return;
        }

        // 데이터 임포트 시작

        final int CATEGORY_CODE = 1;
        final int CATEGORY_NAME = 2;
        final int GROUP_CODE = 3;
        final int GROUP_NAME = 4;
        final int PART_CODE = 5;
        final int PART_NAME = 6;

        ClassPathResource resource = new ClassPathResource("data/parts.csv");

        try (CSVReader reader = new CSVReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readNext();  // 헤더 행 건너뛰기

            String[] line;
            Map<String, PartCategory> categoryCache = new HashMap<>();
            Map<String, PartGroup> groupCache = new HashMap<>();

            while ((line = reader.readNext()) != null) {
                final String[] currentLine = line;

                String categoryCode = currentLine[CATEGORY_CODE];
                PartCategory category = categoryCache.computeIfAbsent(categoryCode, code ->
                        categoryRepository.findByCode(code).orElseGet(() ->
                                categoryRepository.save(new PartCategory(code, currentLine[CATEGORY_NAME]))
                        )
                );

                String groupCode = currentLine[GROUP_CODE];

                String compositeGroupKey = categoryCode + "-" + groupCode;  // 그룹코드만 하면 중복되니깐 '카테고리코드-그룹코드'

                PartGroup partGroup = groupCache.computeIfAbsent(compositeGroupKey, key ->
                        partGroupRepository.findByCodeAndCategory(groupCode, category).orElseGet(() ->
                                partGroupRepository.save(new PartGroup(groupCode, currentLine[GROUP_NAME], category))
                        )
                );

                Part part = new Part(currentLine[PART_CODE], currentLine[PART_NAME], partGroup);
                partRepository.save(part);
            }
        }

        System.out.println(">>>> CSV 데이터 임포트 완료. " + partRepository.count() + "개의 부품이 생성");
    }
}