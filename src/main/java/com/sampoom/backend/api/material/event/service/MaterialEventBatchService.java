package com.sampoom.backend.api.material.event.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampoom.backend.api.material.entity.Material;
import com.sampoom.backend.api.material.entity.MaterialCategory;
import com.sampoom.backend.api.material.event.dto.MaterialEvent;
import com.sampoom.backend.api.material.event.dto.MaterialCategoryEvent;
import com.sampoom.backend.api.material.repository.MaterialCategoryRepository;
import com.sampoom.backend.api.material.repository.MaterialRepository;
import com.sampoom.backend.common.outbox.entity.Outbox;
import com.sampoom.backend.common.outbox.repository.OutboxRepository;
import com.sampoom.backend.common.outbox.entity.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialEventBatchService {

    private final MaterialRepository materialRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final MaterialCategoryRepository materialCategoryRepository;

    public void publishAllMaterialEvents() {
        List<Material> materials = materialRepository.findAll();
        for (Material material : materials) {
            try {
                // 전체 MaterialEvent 객체 생성
                MaterialEvent materialEvent = MaterialEvent.builder()
                        .eventId(java.util.UUID.randomUUID().toString())
                        .eventType("MaterialCreated")
                        .version(material.getVersion())
                        .occurredAt(java.time.OffsetDateTime.now().toString())
                        .payload(MaterialEvent.Payload.builder()
                                .materialId(material.getId())
                                .materialCode(material.getMaterialCode())
                                .name(material.getName())
                                .materialUnit(material.getMaterialUnit())
                                .baseQuantity(material.getBaseQuantity())
                                .standardQuantity(material.getStandardQuantity() != null ? material.getStandardQuantity() : 1)
                                .leadTime(material.getLeadTime())
                                .standardCost(material.getStandardCost())
                                .standardTotalCost(material.getStandardTotalCost())
                                .deleted(false)
                                .materialCategoryId(material.getMaterialCategory().getId())
                                .build())
                        .build();

                // Outbox 엔티티 생성
                Outbox outbox = Outbox.builder()
                        .aggregateType("MATERIAL")
                        .aggregateId(material.getId())
                        .eventType("MaterialCreated")
                        .payload(objectMapper.writeValueAsString(materialEvent))
                        .version(material.getVersion())
                        .occurredAt(OffsetDateTime.now())
                        .status(OutboxStatus.READY)
                        .build();

                outboxRepository.save(outbox);
            } catch (Exception e) {
                // 예외 처리 (로깅 등)
            }
        }
    }

    public void publishAllMaterialCategoryEvents() {
        List<MaterialCategory> categories = materialCategoryRepository.findAll();

        for (MaterialCategory category : categories) {
            try {
                MaterialCategoryEvent.Payload payload = MaterialCategoryEvent.Payload.builder()
                        .categoryId(category.getId())
                        .name(category.getName())
                        .code(category.getCode())
                        .deleted(false) // 필요시 실제 삭제 여부로 변경
                        .build();

                Outbox outbox = Outbox.builder()
                        .aggregateType("MATERIAL_CATEGORY")
                        .aggregateId(category.getId())
                        .eventType("MaterialCategoryCreated")
                        .payload(objectMapper.writeValueAsString(payload))
                        .version(1L)
                        .occurredAt(OffsetDateTime.now())
                        .status(OutboxStatus.READY)
                        .build();

                outboxRepository.save(outbox);
            } catch (Exception e) {
                // 예외 처리 (로깅 등)
            }
        }
    }
}