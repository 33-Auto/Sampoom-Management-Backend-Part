//package com.sampoom.backend.api.bom.event.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.sampoom.backend.api.bom.entity.Bom;
//import com.sampoom.backend.api.bom.event.dto.BomEvent;
//import com.sampoom.backend.common.outbox.entity.Outbox;
//import com.sampoom.backend.common.outbox.repository.OutboxRepository;
//import com.sampoom.backend.common.outbox.entity.OutboxStatus;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.OffsetDateTime;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class BomEventBatchService {
//
//    private final OutboxRepository outboxRepository;
//    private final ObjectMapper objectMapper;
//    private final com.sampoom.backend.api.bom.repository.BomRepository bomRepository;
//
//    @Transactional
//    public void publishAllBomEvents() {
//        List<Bom> boms = bomRepository.findAllWithMaterials();
//        for (Bom bom : boms) {
//            try {
//                // 자재 정보 리스트 구성
//                List<BomEvent.Payload.MaterialInfo> materials = bom.getMaterials().stream()
//                        .map(m -> BomEvent.Payload.MaterialInfo.builder()
//                                .materialId(m.getMaterial().getId())
//                                .materialName(m.getMaterial().getName())
//                                .materialCode(m.getMaterial().getMaterialCode())
//                                .unit(m.getMaterial().getMaterialUnit())
//                                .quantity(m.getQuantity())
//                                .build())
//                        .toList();
//
//                // Payload 생성
//                BomEvent.Payload payload = BomEvent.Payload.builder()
//                        .bomId(bom.getId())
//                        .partId(bom.getPart().getId())
//                        .partCode(bom.getPart().getCode())
//                        .partName(bom.getPart().getName())
//                        .status(bom.getStatus().name())
//                        .complexity(bom.getComplexity().name())
//                        .materials(materials)
//                        .build();
//
//                // Outbox 생성
//                Outbox outbox = Outbox.builder()
//                        .aggregateType("BOM")
//                        .aggregateId(bom.getId())
//                        .eventType("BomCreated")
//                        .payload(objectMapper.writeValueAsString(payload))
//                        .version(bom.getVersion())
//                        .occurredAt(OffsetDateTime.now())
//                        .status(OutboxStatus.READY)
//                        .build();
//
//                outboxRepository.save(outbox);
//            } catch (Exception e) {
//                e.printStackTrace(); // 혹은 log.error("BOM 이벤트 생성 실패", e);
//            }
//        }
//    }
//}
