package com.sampoom.backend.common.outbox.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampoom.backend.api.bom.event.dto.BomEvent;
import com.sampoom.backend.api.material.event.dto.MaterialCategoryEvent;
import com.sampoom.backend.api.part.event.dto.PartCategoryEvent;
import com.sampoom.backend.api.part.event.dto.PartEvent;
import com.sampoom.backend.api.part.event.dto.PartGroupEvent;
import com.sampoom.backend.common.outbox.entity.Outbox;
import com.sampoom.backend.common.outbox.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventProcessor {

    // Kafka 토픽 이름 정의
    private static final String TOPIC_PART = "part-events";
    private static final String TOPIC_PART_GROUP = "part-group-events";
    private static final String TOPIC_PART_CATEGORY = "part-category-events";
    private static final String TOPIC_MATERIAL = "material-events"; // 자재 토픽 추가
    private static final String TOPIC_MATERIAL_CATEGORY = "material-category-events";
    private static final String TOPIC_BOM = "bom-events";

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 개별 Outbox 이벤트 발행
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processAndPublishEvent(Outbox outbox) {
        String topicName;
        Object eventToSend; // Kafka로 보낼 최종 이벤트 DTO
        String eventKey = outbox.getAggregateId().toString();

        try {
            // AggregateType에 따라 DTO 역질렬화 및 토픽/이벤트 구성
            switch (outbox.getAggregateType()) {
                case "PART":
                    eventToSend = objectMapper.readValue(outbox.getPayload(), PartEvent.class);
                    topicName = TOPIC_PART; // Part 토픽
                    break;

                case "PART_GROUP":
                    PartGroupEvent.Payload groupPayload = objectMapper.readValue(outbox.getPayload(), PartGroupEvent.Payload.class);
                    eventToSend = PartGroupEvent.builder()
                            .eventId(outbox.getEventId())
                            .eventType(outbox.getEventType())
                            .version(outbox.getVersion())
                            .occurredAt(outbox.getOccurredAt().toString())
                            .payload(groupPayload)
                            .build();
                    topicName = TOPIC_PART_GROUP;
                    break;

                case "PART_CATEGORY":
                    PartCategoryEvent.Payload categoryPayload = objectMapper.readValue(outbox.getPayload(), PartCategoryEvent.Payload.class);
                    eventToSend = PartCategoryEvent.builder()
                            .eventId(outbox.getEventId())
                            .eventType(outbox.getEventType())
                            .version(outbox.getVersion())
                            .occurredAt(outbox.getOccurredAt().toString())
                            .payload(categoryPayload)
                            .build();
                    topicName = TOPIC_PART_CATEGORY;
                    break;

                case "BOM":
                    BomEvent.Payload bomPayload =
                            objectMapper.readValue(outbox.getPayload(), BomEvent.Payload.class);
                    eventToSend = BomEvent.builder()
                            .eventId(outbox.getEventId())
                            .eventType(outbox.getEventType())
                            .version(outbox.getVersion())
                            .occurredAt(outbox.getOccurredAt().toString())
                            .payload(bomPayload)
                            .build();
                    topicName = TOPIC_BOM;
                    break;

                case "MATERIAL":
                    // 전체 MaterialEvent 객체를 직접 역직렬화
                    eventToSend = objectMapper.readValue(outbox.getPayload(), com.sampoom.backend.api.material.event.dto.MaterialEvent.class);
                    topicName = TOPIC_MATERIAL;
                    break;

                case "MATERIAL_CATEGORY":
                    MaterialCategoryEvent.Payload materialCategoryPayload = objectMapper.readValue(outbox.getPayload(),  MaterialCategoryEvent.Payload.class);
                    eventToSend = com.sampoom.backend.api.material.event.dto.MaterialCategoryEvent.builder()
                            .eventId(outbox.getEventId())
                            .eventType(outbox.getEventType())
                            .version(outbox.getVersion())
                            .occurredAt(outbox.getOccurredAt().toString())
                            .payload(materialCategoryPayload)
                            .build();
                    topicName = TOPIC_MATERIAL_CATEGORY;
                    break;


                default:
                    throw new IllegalStateException("알 수 없는 AggregateType: " + outbox.getAggregateType());
            }

            // Kafka 발행 (동기식 처리)
            // Kafka가 "잘 받았다"고 응답할 때까지 10초간 기다립니다.
            kafkaTemplate.send(topicName, eventKey, eventToSend).get(10, TimeUnit.SECONDS);

            // 발행 성공 시 처리 (같은 트랜잭션)
            outbox.markPublished();
            outboxRepository.save(outbox);
            log.info("[OutboxEvent] Kafka 발행 성공: {} ({})", outbox.getEventType(), outbox.getEventId());

        } catch (Exception e) {
            // Kafka 발행 실패 또는 DB 업데이트 실패 시
            outbox.markFailed();
            outboxRepository.save(outbox);
            log.error("[OutboxEvent] 발행 실패 (FAILED 처리): eventId={}, reason={}", outbox.getEventId(), e.getMessage());
        }
    }
}
