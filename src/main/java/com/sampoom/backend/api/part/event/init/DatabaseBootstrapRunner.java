//package com.sampoom.backend.api.part.event.init; // (패키지는 적절히)
//
//import com.sampoom.backend.api.part.entity.Part;
//import com.sampoom.backend.api.part.entity.PartCategory;
//import com.sampoom.backend.api.part.entity.PartGroup;
//import com.sampoom.backend.api.part.event.dto.PartCategoryEvent;
//import com.sampoom.backend.api.part.event.dto.PartEvent;
//import com.sampoom.backend.api.part.event.dto.PartGroupEvent;
//import com.sampoom.backend.api.part.event.repository.OutboxRepository;
//import com.sampoom.backend.api.part.event.service.OutboxService;
//import com.sampoom.backend.api.part.repository.PartCategoryRepository;
//import com.sampoom.backend.api.part.repository.PartGroupRepository;
//import com.sampoom.backend.api.part.repository.PartRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class DatabaseBootstrapRunner implements CommandLineRunner {
//
//    private final PartRepository partRepository;
//    private final PartGroupRepository partGroupRepository;
//    private final PartCategoryRepository categoryRepository;
//
//    private final OutboxRepository outboxRepository;
//    private final OutboxService outboxService;
//
//    @Override
//    @Transactional
//    public void run(String... args) {
//        log.info("[INIT] 기존 마스터 데이터 Outbox 저장 시작...");
//
//        // ⭐️ 순서가 중요합니다: Category -> Group -> Part
//        int categoryCount = bootstrapCategories();
//        int groupCount = bootstrapPartGroups();
//        int partCount = bootstrapParts();
//
//        log.info("✅ [INIT] Outbox 저장 완료! (Category: {}개, Group: {}개, Part: {}개 추가)",
//                categoryCount, groupCount, partCount);
//    }
//
//    private int bootstrapCategories() {
//        log.info("[INIT] 1. PartCategory 데이터 저장 중...");
//        List<PartCategory> allCategories = categoryRepository.findAll();
//        int inserted = 0;
//
//        for (PartCategory category : allCategories) {
//            boolean alreadyExists = outboxRepository.existsByAggregateIdAndAggregateType(
//                    category.getId(), "PART_CATEGORY");
//
//            if (alreadyExists) continue;
//
//            PartCategoryEvent.Payload payload = PartCategoryEvent.Payload.builder()
//                    .categoryId(category.getId())
//                    .categoryName(category.getName())
//                    .categoryCode(category.getCode())
//                    .build();
//
//            outboxService.saveEvent(
//                    "PART_CATEGORY",
//                    category.getId(),
//                    "PartCategoryCreated", // (이벤트 타입을 "Created"로 통일)
//                    category.getVersion(),
//                    payload
//            );
//            inserted++;
//        }
//        return inserted;
//    }
//
//    private int bootstrapPartGroups() {
//        log.info("[INIT] PartGroup 데이터 저장 중...");
//        // ⭐️ N+1 방지를 위해 Fetch Join 메서드 사용
//        List<PartGroup> allGroups = partGroupRepository.findAllForBootstrap();
//        int inserted = 0;
//
//        for (PartGroup group : allGroups) {
//            boolean alreadyExists = outboxRepository.existsByAggregateIdAndAggregateType(
//                    group.getId(), "PART_GROUP");
//
//            if (alreadyExists) continue;
//
//            PartGroupEvent.Payload payload = PartGroupEvent.Payload.builder()
//                    .groupId(group.getId())
//                    .groupName(group.getName())
//                    .groupCode(group.getCode())
//                    .categoryId(group.getCategory().getId())
//                    .build();
//
//            outboxService.saveEvent(
//                    "PART_GROUP",
//                    group.getId(),
//                    "PartGroupCreated",
//                    group.getVersion(),
//                    payload
//            );
//            inserted++;
//        }
//        return inserted;
//    }
//
//    private int bootstrapParts() {
//        log.info("[INIT] Part 데이터 저장 중... (총 700개 이상)");
//        // ⭐️ N+1 방지를 위해 Fetch Join 메서드 사용
//        List<Part> allParts = partRepository.findAllForBootstrap();
//        int inserted = 0;
//
//        for (Part part : allParts) {
//            boolean alreadyExists = outboxRepository.existsByAggregateIdAndAggregateType(
//                    part.getId(), "PART");
//
//            if (alreadyExists) continue;
//
//            PartEvent.Payload payload = PartEvent.Payload.builder()
//                    .partId(part.getId())
//                    .code(part.getCode())
//                    .name(part.getName())
//                    .status(part.getStatus().name())
//                    .deleted(false)
//                    .groupId(part.getPartGroup().getId()) // N+1 방지됨
//                    .categoryId(part.getPartGroup().getCategory().getId()) // N+1 방지됨
//                    .build();
//
//            outboxService.saveEvent(
//                    "PART",
//                    part.getId(),
//                    "PartCreated",
//                    part.getVersion(),
//                    payload
//            );
//            inserted++;
//        }
//        return inserted;
//    }
//}