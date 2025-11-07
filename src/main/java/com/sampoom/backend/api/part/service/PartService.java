package com.sampoom.backend.api.part.service;

import com.sampoom.backend.api.part.dto.*;
import com.sampoom.backend.api.part.entity.*;
import com.sampoom.backend.api.part.event.dto.PartEvent;
import com.sampoom.backend.common.outbox.service.OutboxService;
import com.sampoom.backend.api.part.repository.PartCategoryRepository;
import com.sampoom.backend.api.part.repository.PartGroupRepository;
import com.sampoom.backend.api.part.repository.PartRepository;
import com.sampoom.backend.api.process.entity.Process;
import com.sampoom.backend.api.process.repository.ProcessRepository;
import com.sampoom.backend.api.bom.entity.Bom;
import com.sampoom.backend.api.bom.repository.BomRepository;
import com.sampoom.backend.common.dto.PageResponseDTO;
import com.sampoom.backend.common.exception.NotFoundException;
import com.sampoom.backend.common.response.ErrorStatus;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class PartService {

    private final PartRepository partRepository;
    private final PartGroupRepository partGroupRepository;
    private final PartCategoryRepository categoryRepository;
    private final OutboxService outboxService;
    private final ProcessRepository processRepository;
    private final BomRepository bomRepository;

    // 카테고리 목록 조회
    @Transactional
    public List<PartCategoryResponseDTO> findAllCategories() {

        List<PartCategory> categories = categoryRepository.findAll();

        return categories.stream()
                .map(PartCategoryResponseDTO::new)
                .collect(Collectors.toList());
    }

    // 카테고리별 그룹 목록 조회
    @Transactional
    public List<PartGroupResponseDTO> findGroupsByCategoryId(Long categoryId) {

        List<PartGroup> partGroups = partGroupRepository.findByCategoryId(categoryId);

        return partGroups.stream()
                .map(PartGroupResponseDTO::new)
                .collect(Collectors.toList());
    }

    // 카테고리에 속한 모든 그룹 부품 목록 조회
    @Transactional(readOnly = true)
    public PageResponseDTO<PartListResponseDTO> findAllPartsByCategory(Long categoryId, int page, int size) {

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Part> partsPage = partRepository.findByPartGroupCategoryIdAndStatus(categoryId, PartStatus.ACTIVE, pageRequest);

        List<PartListResponseDTO> dtoList = partsPage.getContent().stream()
                .map(PartListResponseDTO::new)
                .toList();

        return PageResponseDTO.<PartListResponseDTO>builder()
                .content(dtoList)
                .totalElements(partsPage.getTotalElements())
                .totalPages(partsPage.getTotalPages())
                .currentPage(page)
                .pageSize(size)
                .build();
    }

    // 그룹별 부품 목록 조회
    @Transactional
    public PageResponseDTO<PartListResponseDTO> findPartsByGroup(Long groupId, int page, int size) {

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Part> parts = partRepository.findByPartGroupId(groupId, pageRequest);

        List<PartListResponseDTO> dtoList = parts.stream().map(PartListResponseDTO::new).toList();
        return PageResponseDTO.<PartListResponseDTO>builder()
                .content(dtoList)
                .totalElements(parts.getTotalElements())
                .totalPages(parts.getTotalPages())
                .currentPage(page)
                .pageSize(size)
                .build();
    }

    // 신규 부품 등록
    @Transactional
    public PartListResponseDTO createPart(PartCreateRequestDTO partCreateRequestDTO) {

        // DTO에 담겨온 groupId로 PartGroup 엔티티 조회
        PartGroup partGroup = partGroupRepository.findById(partCreateRequestDTO.getGroupId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.GROUP_NOT_FOUND));

        // 코드 자동 생성 (처음 1회)
        String nextCode = generateNextPartCode(partGroup.getId());

        // 재시도 로직
        int attempts = 0;
        final int MAX_ATTEMPTS = 3;  // 최대 3번 재시도
        Part savedPart;

        while (true) {

            try {
                // 부품 생성 시도
                Part newPart = new Part(
                        nextCode,
                        partCreateRequestDTO.getName(),
                        partGroup,
                        partCreateRequestDTO.getPartUnit(),
                        partCreateRequestDTO.getBaseQuantity(),
                        partCreateRequestDTO.getStandardQuantity() != null ?
                            partCreateRequestDTO.getStandardQuantity() : 1
                );

                // 표준단가 자동 계산

                savedPart = partRepository.saveAndFlush(newPart);
                break;

            } catch (DataIntegrityViolationException e) {
                // DataIntegrityViolationException 감지 (코드 중복 의심)
                log.warn("DataIntegrityViolationException 감지 (코드 중복 가능성): {}", e.getMessage());

                if (++attempts >= MAX_ATTEMPTS) {
                    log.error("부품 코드 생성 3회 재시도 실패 (partGroup: {}).", partGroup.getId());
                    // 3번 다 실패하면 그냥 예외를 던져서 500 에러 처리
                    throw new RuntimeException("부품 코드 생성에 실패했습니다.", e);
                }

                // 중복이었으므로, 새 코드를 다시 받아옴
                log.info("부품 코드 중복 감지, 새 코드 생성 재시도... (시도: {}/{} )", attempts, MAX_ATTEMPTS);
                nextCode = generateNextPartCode(partGroup.getId());
            }
        }

        // 전체 PartEvent 객체 생성
        PartEvent partEvent = PartEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("PartCreated")
                .version(savedPart.getVersion())
                .occurredAt(java.time.OffsetDateTime.now().toString())
                .payload(PartEvent.Payload.builder()
                        .partId(savedPart.getId())
                        .code(savedPart.getCode())
                        .name(savedPart.getName())
                        .partUnit(savedPart.getPartUnit())
                        .baseQuantity(savedPart.getBaseQuantity())
                        .standardQuantity(savedPart.getStandardQuantity())
                        .leadTime(savedPart.getLeadTime())
                        .status(savedPart.getStatus().name())
                        .deleted(false)
                        .groupId(partGroup.getId())
                        .categoryId(partGroup.getCategory().getId())
                        .standardCost(savedPart.getStandardCost())
                        .standardTotalCost(savedPart.getStandardTotalCost())
                        .build())
                .build();

        // OutboxService 호출 (전체 이벤트 객체)
        outboxService.saveEvent(
                "PART",
                savedPart.getId(),
                "PartCreated",
                savedPart.getVersion(),
                partEvent.getPayload()
        );

        return new PartListResponseDTO(savedPart);
    }

    // 부품 수정
    @Transactional
    public PartListResponseDTO updatePart(Long partId, PartUpdateRequestDTO partUpdateRequestDTO) {

        try {
            // 수정할 부품을 조회
            Part part = partRepository.findById(partId)
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.PART_NOT_FOUND));

            // 그룹 변경 시 코드 재생성
            if (partUpdateRequestDTO.getGroupId() != null && !partUpdateRequestDTO.getGroupId().equals(part.getPartGroup().getId())) {
                PartGroup newGroup = partGroupRepository.findById(partUpdateRequestDTO.getGroupId())
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.GROUP_NOT_FOUND));

                part.changeGroup(newGroup); // 엔티티에 추가할 메서드

                // 코드 재생성
                String newCode = generateNextPartCode(newGroup.getId());
                part.changeCode(newCode);
            }

            part.update(partUpdateRequestDTO);

            // 표준단가 계산

            partRepository.flush();

            // 전체 PartEvent 객체 생성
            PartEvent partEvent = PartEvent.builder()
                    .eventId(java.util.UUID.randomUUID().toString())
                    .eventType("PartUpdated")
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
                            .groupId(part.getPartGroup().getId())
                            .categoryId(part.getPartGroup().getCategory().getId())
                            .standardCost(part.getStandardCost())
                            .standardTotalCost(part.getStandardTotalCost())
                            .build())
                    .build();

            // OutboxService 호출
            outboxService.saveEvent(
                    "PART",
                    part.getId(),
                    "PartUpdated",
                    part.getVersion(),
                    partEvent.getPayload()
            );

            return new PartListResponseDTO(part);

        } catch (OptimisticLockException e) {
            log.warn("Part 동시 수정 충돌 감지 (partId: {}): {}", partId, e.getMessage());
            throw e;
        }
    }

    // 부품 삭제
    @Transactional
    public void deletePart(Long partId) {

        // 삭제할 부품 조회
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.PART_NOT_FOUND));

        // Soft Delete 메서드 호출
        part.delete();

        partRepository.flush();

        // 전체 PartEvent 객체 생성
        PartEvent partEvent = PartEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("PartDeleted")
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
                        .status(part.getStatus().name()) // "DISCONTINUED"
                        .deleted(true)
                        .groupId(part.getPartGroup().getId())
                        .categoryId(part.getPartGroup().getCategory().getId())
                        .standardCost(part.getStandardCost())
                        .standardTotalCost(part.getStandardTotalCost())
                        .build())
                .build();

        // OutboxService 호출 (전체 이벤트 객체)
        outboxService.saveEvent(
                "PART",
                part.getId(),
                "PartDeleted",
                part.getVersion(),
                partEvent.getPayload()
        );
    }

    // 부품 검색
    @Transactional(readOnly = true)
    public PageResponseDTO<PartListResponseDTO> searchParts(
            String keyword,
            Long categoryId,
            Long groupId,
            int page,
            int size
    ) {
        // 코드순 정렬 추가
        PageRequest pageable = PageRequest.of(page, size, Sort.by("code").ascending());

        Page<Part> parts = partRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 상태 필터
            predicates.add(cb.equal(root.get("status"), PartStatus.ACTIVE));

            // keyword 검색
            if (keyword != null && !keyword.isBlank()) {
                Predicate nameLike = cb.like(root.get("name"), "%" + keyword + "%");
                Predicate codeLike = cb.like(root.get("code"), "%" + keyword + "%");
                predicates.add(cb.or(nameLike, codeLike));
            }

            // 카테고리 필터
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("partGroup").get("category").get("id"), categoryId));
            }

            // 그룹 필터
            if (groupId != null) {
                predicates.add(cb.equal(root.get("partGroup").get("id"), groupId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        List<PartListResponseDTO> dtoList = parts.getContent().stream()
                .map(PartListResponseDTO::new)
                .toList();

        return PageResponseDTO.<PartListResponseDTO>builder()
                .content(dtoList)
                .totalElements(parts.getTotalElements())
                .totalPages(parts.getTotalPages())
                .currentPage(page)
                .pageSize(size)
                .build();
    }


    // 코드 생성
    @Transactional(readOnly = true)
    public String generateNextPartCode(Long groupId) {

        PartGroup group = partGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.GROUP_NOT_FOUND));

        String categoryCode = group.getCategory().getCode(); // ENG, TRM 등
        String groupCode = String.format("%02d", group.getId()); // 그룹 ID 두 자리로 포맷

        // 최신 부품 코드 조회 (code 순 정렬 기준)
        Part latest = partRepository.findTopByPartGroupIdOrderByCodeDesc(groupId);

        int nextSeq = 1;
        if (latest != null && latest.getCode() != null) {
            String[] parts = latest.getCode().split("-");
            if (parts.length == 3) {
                try {
                    nextSeq = Integer.parseInt(parts[2]) + 1;
                } catch (NumberFormatException ignored) {}
            }
        }

        return String.format("%s-%s-%03d", categoryCode, groupCode, nextSeq);
    }

    @Transactional
    public void updateLeadTimeFromProcess(Long partId) {

        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.PART_NOT_FOUND));

        // Process Master 조회
        Process process = processRepository.findByPartId(partId)
                .orElse(null);

        // Lead Time 계산 (일 단위 + 배송시간 2일)
        Integer newLeadTime = 2; // 기본 배송시간 2일
        if (process != null) {
            int totalMinutes = process.getTotalStepMinutes();
            // 분을 일로 변환 (1일 = 24시간 = 1440분)
            // 올림 처리하여 최소 1일은 보장
            int processLeadTimeDays = (int) Math.ceil(totalMinutes / 1440.0);
            newLeadTime = processLeadTimeDays + 2; // 공정시간(일) + 배송시간 2일

            log.debug("Part [{}] leadTime 계산: 공정시간={}분({}일) + 배송시간=2일 = 총 {}일",
                     part.getCode(), totalMinutes, processLeadTimeDays, newLeadTime);
        }

        // Part 엔티티 업데이트 및 이벤트 발행 (변경이 있을 경우에만)
        if (part.getLeadTime() == null || !part.getLeadTime().equals(newLeadTime)) {
            part.setLeadTime(newLeadTime);

            // DB에 변경사항 반영 (@Version 증가)
            partRepository.flush();

            // Kafka 이벤트 발행
            publishPartUpdatedEvent(part);
        }
    }

    // 이벤트 발행 헬퍼 메서드
    @Transactional(readOnly = true)
    public void publishPartUpdatedEvent(Part part) {
        // 전체 PartEvent 객체 생성
        PartEvent partEvent = PartEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("PartUpdated")
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
                        .deleted(part.getStatus() == PartStatus.DISCONTINUED)
                        .groupId(part.getPartGroup().getId())
                        .categoryId(part.getPartGroup().getCategory().getId())
                        .standardCost(part.getStandardCost())
                        .standardTotalCost(part.getStandardTotalCost())
                        .build())
                .build();

        outboxService.saveEvent(
                "PART",
                part.getId(),
                "PartUpdated",
                part.getVersion(),
                partEvent.getPayload()
        );
    }

    // Part의 표준 비용을 BOM 비용과 Process 비용을 합쳐서 자동 계산하는 메서드
    @Transactional
    public void updateStandardCostFromBomAndProcess(Long partId) {
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.PART_NOT_FOUND));

        // BOM 비용 조회
        Long bomCost = getBomCostByPartId(partId);

        // Process 비용 조회
        Long processCost = getProcessCostByPartId(partId);

        // 기존 표준 비용과 비교해서 변경이 있을 경우에만 업데이트
        Long newStandardCost = (bomCost != null ? bomCost : 0L) + (processCost != null ? processCost : 0L);

        if (part.getStandardCost() == null || !part.getStandardCost().equals(newStandardCost)) {
            part.calculateStandardCost(bomCost, processCost);

            // DB에 변경사항 반영
            partRepository.flush();

            // Kafka 이벤트 발행
            publishPartUpdatedEvent(part);
        }
    }

    // BOM 비용 조회 헬퍼 메서드
    @Transactional(readOnly = true)
    public Long getBomCostByPartId(Long partId) {
        Bom bom = bomRepository.findByPart_Id(partId).orElse(null);
        return bom != null ? bom.getTotalCost() : 0L;
    }

    // Process 비용 조회 헬퍼 메서드
    @Transactional(readOnly = true)
    public Long getProcessCostByPartId(Long partId) {
        Process process = processRepository.findByPartId(partId).orElse(null);
        return process != null ? process.getTotalProcessCost() : 0L;
    }


    // 부품 상세조회
    @Transactional(readOnly = true)
    public PartListResponseDTO getPartById(Long partId) {
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.PART_NOT_FOUND));

        return new PartListResponseDTO(part);
    }
  
    /**
     * 모든 Part의 standard_total_cost 재계산
     */
    @Transactional
    public void recalculateAllPartStandardCosts() {
        List<Part> parts = partRepository.findAll();
        for (Part part : parts) {
            Long bomCost = getBomCostByPartId(part.getId());
            Long processCost = getProcessCostByPartId(part.getId());
            part.calculateStandardCost(bomCost, processCost);
        }
        partRepository.saveAll(parts);
    }

    /**
     * 특정 Part의 standard_total_cost 재계산
     */
    @Transactional
    public void recalculatePartStandardCost(Long partId) {
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.PART_NOT_FOUND));

        Long bomCost = getBomCostByPartId(partId);
        Long processCost = getProcessCostByPartId(partId);
        part.calculateStandardCost(bomCost, processCost);

        partRepository.save(part);
    }
}
