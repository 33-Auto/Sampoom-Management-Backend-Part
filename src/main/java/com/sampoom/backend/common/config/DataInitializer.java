package com.sampoom.backend.common.config;

import com.opencsv.CSVReader;
import com.sampoom.backend.api.material.entity.Material;
import com.sampoom.backend.api.material.entity.MaterialCategory;
import com.sampoom.backend.api.material.repository.MaterialCategoryRepository;
import com.sampoom.backend.api.material.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class DataInitializer implements CommandLineRunner {

    private final MaterialRepository materialRepository;
    private final MaterialCategoryRepository categoryRepository;

    // CSV의 카테고리명 → 카테고리 코드(prefix) 매핑
    private static final Map<String, String> CATEGORY_PREFIX = Map.of(
            "금속", "MTL",
            "플라스틱/고무", "PLS",
            "전기전자", "ELC",
            "화학/소모품", "CHM"
    );

    @Override
    public void run(String... args) throws Exception {
        if (materialRepository.count() > 0) {
            log.info("Material data already exists, skipping import.");
            return;
        }

        log.info("Importing CSV data into PostgreSQL...");

        try (Reader reader = new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/data/materials_master.csv")),
                StandardCharsets.UTF_8
        );
             CSVReader csvReader = new CSVReader(reader)) {

            List<String[]> rows = csvReader.readAll();

            if (!rows.isEmpty()) rows.remove(0); // 헤더 제거

            Map<String, MaterialCategory> categoryCache = new HashMap<>();

            List<Material> toSave = new ArrayList<>(rows.size());

            for (String[] row : rows) {
                // CSV 스키마: id, category_id, category, code, name, unit
                // String csvId = row[0];           // 사용 안 함
                // String csvCategoryId = row[1];   // 사용 안 함 (DB PK랑 불일치 가능)
                String categoryName = row[2];
                String code = row[3];
                String name = row[4];
                String unitStr = row[5]; // "kg", "m" 등

                // 1) 카테고리 upsert (code 기준)
                String prefix = CATEGORY_PREFIX.getOrDefault(categoryName, "CAT");
                MaterialCategory category = categoryCache.get(prefix);
                if (category == null) {
                    category = categoryRepository.findByCode(prefix).orElseGet(() ->
                            categoryRepository.save(
                                    MaterialCategory.builder()
                                            .name(categoryName)
                                            .code(prefix)
                                            .build()
                            )
                    );
                    categoryCache.put(prefix, category);
                }

                // 2) 자재 엔티티 생성
                Material material = Material.builder()
                        .name(name)
                        .materialCode(code)
                        .materialUnit(unitStr)
                        .materialCategory(category)
                        .build();

                toSave.add(material);

            }

            materialRepository.saveAll(toSave);

            log.info("CSV import completed. Inserted materials: " + materialRepository.count());
        }
    }
}
