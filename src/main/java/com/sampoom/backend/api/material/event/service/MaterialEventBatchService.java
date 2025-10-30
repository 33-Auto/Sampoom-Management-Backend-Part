package com.sampoom.backend.api.material.event.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampoom.backend.api.material.entity.Material;
import com.sampoom.backend.api.material.entity.MaterialCategory;
import com.sampoom.backend.api.material.event.dto.MaterialEvent;
import com.sampoom.backend.api.material.event.dto.MaterialCategoryEvent;
import com.sampoom.backend.api.material.repository.MaterialCategoryRepository;
import com.sampoom.backend.api.material.repository.MaterialRepository;
import com.sampoom.backend.api.part.event.entity.Outbox;
import com.sampoom.backend.api.part.event.repository.OutboxRepository;
import com.sampoom.backend.common.entity.OutboxStatus;
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
                // MaterialEvent.Payload 생성 (필요에 따라 필드 매핑)
                MaterialEvent.Payload payload = MaterialEvent.Payload.builder()
                        .materialId(material.getId())
                        .materialCode(material.getMaterialCode())
                        .name(material.getName())
                        .materialUnit(material.getMaterialUnit())
                        .baseQuantity(material.getBaseQuantity())
                        .leadTime(material.getLeadTime())
                        .deleted(false)
                        .materialCategoryId(material.getMaterialCategory().getId())

                        // ... 필요한 필드 추가 ...
                        .build();

                // Outbox 엔티티 생성
                Outbox outbox = Outbox.builder()
                        .aggregateType("MATERIAL")
                        .aggregateId(material.getId())
                        .eventType("MaterialCreated")
                        .payload(objectMapper.writeValueAsString(payload))
                        .version(material.getVersion())
                        .occurredAt(OffsetDateTime.now())
                        .status(OutboxStatus.READY) // 상태 필드명에 맞게 수정
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