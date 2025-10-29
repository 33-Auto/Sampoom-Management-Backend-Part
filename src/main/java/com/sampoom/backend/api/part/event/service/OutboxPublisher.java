package com.sampoom.backend.api.part.event.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampoom.backend.api.part.event.dto.PartCategoryEvent;
import com.sampoom.backend.api.part.event.dto.PartEvent;
import com.sampoom.backend.api.part.event.dto.PartGroupEvent;
import com.sampoom.backend.api.part.event.entity.Outbox;
import com.sampoom.backend.api.part.event.repository.OutboxRepository;
import com.sampoom.backend.common.entitiy.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxPublisher {

    // Kafka 토픽 이름 정의
    private static final String TOPIC_PART = "part-events";
    private static final String TOPIC_PART_GROUP = "part-group-events";
    private static final String TOPIC_PART_CATEGORY = "part-category-events";

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * READY 상태 Outbox 5초마다 Kafka로 발행
     */
    @Scheduled(fixedDelay = 5000)
    public void publishReadyEvents() {
        List<Outbox> events = outboxRepository.findTop10ByStatusOrderByCreatedAtAsc(OutboxStatus.READY);

        if (events.isEmpty()) return;

        log.info("[PartOutboxPublisher] 발행할 이벤트 {}개 발견", events.size());

        for (Outbox outbox : events) {
            try {
                // 개별 트랜잭션으로 처리하기 위해 public 메서드 호출
                processAndPublishEvent(outbox);
            } catch (Exception e) {
                log.error("[PartOutboxPublisher] 이벤트 처리 중 심각한 오류 발생 (트랜잭션 롤백됨): eventId={}, reason={}",
                        outbox.getEventId(), e.getMessage());
            }
        }
    }

    /**
     * 개별 Outbox 이벤트 발행
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processAndPublishEvent(Outbox outbox) {
        String topicName;
        Object eventToSend; // Kafka로 보낼 최종 이벤트 DTO

        try {
            // AggregateType에 따라 DTO 역질렬화 및 토픽/이벤트 구성
            switch (outbox.getAggregateType()) {
                case "PART":
                    PartEvent.Payload partPayload = objectMapper.readValue(outbox.getPayload(), PartEvent.Payload.class);
                    eventToSend = PartEvent.builder()
                            .eventId(outbox.getEventId())
                            .eventType(outbox.getEventType())
                            .version(outbox.getVersion())
                            .occurredAt(outbox.getOccurredAt().toString())
                            .payload(partPayload)
                            .build();
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

                default:
                    throw new IllegalStateException("알 수 없는 AggregateType: " + outbox.getAggregateType());
            }

            // Kafka 발행 (비동기식 처리)
            kafkaTemplate.send(topicName, eventToSend)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            // 성공 시 처리
                            outbox.markPublished();
                            outboxRepository.save(outbox);
                            log.info("Kafka 발행 성공: {}", result.getProducerRecord().value());
                        } else {
                            // 실패 시 처리
                            outbox.markFailed();
                            outboxRepository.save(outbox);
                            log.error("Kafka 발행 실패: {}", ex.getMessage());
                        }
                    });

            log.info("[PartEvent] Kafka 발행 성공: {} ({})", outbox.getEventType(), outbox.getEventId());

        } catch (Exception e) {
            // Kafka 발행 실패 또는 DB 업데이트 실패 시
            outbox.markFailed();
            outboxRepository.save(outbox);
            log.error("[PartEvent] 발행 실패 (트랜잭션 롤백): eventId={}, reason={}", outbox.getEventId(), e.getMessage());
        }
    }
}