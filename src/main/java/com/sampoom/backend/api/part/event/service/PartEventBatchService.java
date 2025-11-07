package com.sampoom.backend.api.part.event.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampoom.backend.api.part.entity.Part;
import com.sampoom.backend.api.part.entity.PartCategory;
import com.sampoom.backend.api.part.entity.PartGroup;
import com.sampoom.backend.api.part.event.dto.PartCategoryEvent;
import com.sampoom.backend.api.part.event.dto.PartEvent;
import com.sampoom.backend.api.part.event.dto.PartGroupEvent;
import com.sampoom.backend.api.part.repository.PartCategoryRepository;
import com.sampoom.backend.api.part.repository.PartGroupRepository;
import com.sampoom.backend.api.part.repository.PartRepository;
import com.sampoom.backend.common.outbox.entity.Outbox;
import com.sampoom.backend.common.outbox.repository.OutboxRepository;
import com.sampoom.backend.common.outbox.entity.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartEventBatchService {

    private final PartRepository partRepository;
    private final PartGroupRepository partGroupRepository;
    private final PartCategoryRepository partCategoryRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    /**
     * ✅ 모든 Part 데이터를 Outbox로 등록 (초기 발행용)
     */
    public void publishAllPartEvents() {
        List<Part> parts = partRepository.findAllWithGroupAndCategory();

        for (Part part : parts) {
            try {
                var group = part.getPartGroup();
                var category = (group != null) ? group.getCategory() : null;

                // 전체 PartEvent 객체 생성
                PartEvent partEvent = PartEvent.builder()
                        .eventId(java.util.UUID.randomUUID().toString())
                        .eventType("PartCreated")
                        .version(part.getVersion())
                        .occurredAt(java.time.OffsetDateTime.now().toString())
                        .payload(PartEvent.Payload.builder()
                                .partId(part.getId())
                                .code(part.getCode())
                                .name(part.getName())
                                .partUnit(part.getPartUnit())
                                .baseQuantity(part.getBaseQuantity())
                                .standardQuantity(part.getStandardQuantity() != null ? part.getStandardQuantity() : 1)
                                .leadTime(part.getLeadTime())
                                .status(part.getStatus().name())
                                .deleted(false)
                                .groupId(group != null ? group.getId() : null)
                                .categoryId(category != null ? category.getId() : null)
                                .standardCost(part.getStandardCost())
                                .build())
                        .build();

                Outbox outbox = Outbox.builder()
                        .aggregateType("PART")
                        .aggregateId(part.getId())
                        .eventType("PartCreated")
                        .payload(objectMapper.writeValueAsString(partEvent))
                        .version(part.getVersion())
                        .occurredAt(OffsetDateTime.now())
                        .status(OutboxStatus.READY)
                        .build();

                outboxRepository.save(outbox);
            } catch (Exception e) {
                log.error("❌ Part 이벤트 생성 실패 (id={}): {}", part.getId(), e.getMessage());
            }
        }

        log.info("✅ 모든 Part 이벤트 Outbox 등록 완료 ({}건)", parts.size());
    }

    /**
     * ✅ 모든 그룹 이벤트 등록
     */
    public void publishAllPartGroupEvents() {
        List<PartGroup> groups = partGroupRepository.findAll();

        for (PartGroup group : groups) {
            try {
                PartGroupEvent.Payload payload = PartGroupEvent.Payload.builder()
                        .groupId(group.getId())
                        .groupName(group.getName())
                        .groupCode(group.getCode())
                        .categoryId(group.getCategory().getId())
                        .build();

                Outbox outbox = Outbox.builder()
                        .aggregateType("PART_GROUP")
                        .aggregateId(group.getId())
                        .eventType("PartGroupCreated")
                        .payload(objectMapper.writeValueAsString(payload))
                        .version(group.getVersion())
                        .occurredAt(OffsetDateTime.now())
                        .status(OutboxStatus.READY)
                        .build();

                outboxRepository.save(outbox);
            } catch (Exception e) {
                log.error("❌ PartGroup 이벤트 생성 실패 (id={}): {}", group.getId(), e.getMessage());
            }
        }

        log.info("✅ 모든 PartGroup 이벤트 Outbox 등록 완료 ({}건)", groups.size());
    }

    /**
     * ✅ 모든 카테고리 이벤트 등록
     */
    public void publishAllPartCategoryEvents() {
        List<PartCategory> categories = partCategoryRepository.findAll();

        for (PartCategory category : categories) {
            try {
                PartCategoryEvent.Payload payload = PartCategoryEvent.Payload.builder()
                        .categoryId(category.getId())
                        .categoryName(category.getName())
                        .categoryCode(category.getCode())
                        .build();

                Outbox outbox = Outbox.builder()
                        .aggregateType("PART_CATEGORY")
                        .aggregateId(category.getId())
                        .eventType("PartCategoryCreated")
                        .payload(objectMapper.writeValueAsString(payload))
                        .version(category.getVersion())
                        .occurredAt(OffsetDateTime.now())
                        .status(OutboxStatus.READY)
                        .build();

                outboxRepository.save(outbox);
            } catch (Exception e) {
                log.error("❌ PartCategory 이벤트 생성 실패 (id={}): {}", category.getId(), e.getMessage());
            }
        }

        log.info("✅ 모든 PartCategory 이벤트 Outbox 등록 완료 ({}건)", categories.size());
    }
}
