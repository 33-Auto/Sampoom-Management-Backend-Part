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
                        partCreateRequestDTO.getBaseQuantity()
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

        PartEvent.Payload payload = PartEvent.Payload.builder()
                .partId(savedPart.getId())
                .code(savedPart.getCode())
                .name(savedPart.getName())
                .partUnit(savedPart.getPartUnit())
                .baseQuantity(savedPart.getBaseQuantity())
                .leadTime(savedPart.getLeadTime())
                .status(savedPart.getStatus().name())
                .deleted(false)
                .groupId(partGroup.getId())
                .categoryId(partGroup.getCategory().getId())
                .standardCost(savedPart.getStandardCost())
                .build();

        // OutboxService 호출
        outboxService.saveEvent(
                "PART",
                savedPart.getId(),
                "PartCreated",
                savedPart.getVersion(),
                payload
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

            PartEvent.Payload payload = PartEvent.Payload.builder()
                    .partId(part.getId())
                    .code(part.getCode())
                    .name(part.getName())
                    .partUnit(part.getPartUnit())
                    .baseQuantity(part.getBaseQuantity())
                    .leadTime(part.getLeadTime())
                    .status(part.getStatus().name())
                    .deleted(false)
                    .groupId(part.getPartGroup().getId())
                    .categoryId(part.getPartGroup().getCategory().getId())
                    .standardCost(part.getStandardCost())
                    .build();

            // OutboxService 호출
            outboxService.saveEvent(
                    "PART",
                    part.getId(),
                    "PartUpdated",
                    part.getVersion(),
                    payload
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

        PartEvent.Payload payload = PartEvent.Payload.builder()
                .partId(part.getId())
                .code(part.getCode())
                .name(part.getName())
                .partUnit(part.getPartUnit())
                .baseQuantity(part.getBaseQuantity())
                .leadTime(part.getLeadTime())
                .status(part.getStatus().name()) // "DISCONTINUED"
                .deleted(true)
                .groupId(part.getPartGroup().getId())
                .categoryId(part.getPartGroup().getCategory().getId())
                .standardCost(part.getStandardCost())
                .build();

        // OutboxService 호출
        outboxService.saveEvent(
                "PART",
                part.getId(),
                "PartDeleted",
                part.getVersion(),
                payload
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
        PageRequest pageable = PageRequest.of(page, size);

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

        // Lead Time 계산
        Integer newLeadTime = 0;
        if (process != null) {
            newLeadTime = process.getTotalStepMinutes();
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
        PartEvent.Payload payload = PartEvent.Payload.builder()
                .partId(part.getId())
                .code(part.getCode())
                .name(part.getName())
                .partUnit(part.getPartUnit())
                .baseQuantity(part.getBaseQuantity())
                .leadTime(part.getLeadTime())
                .status(part.getStatus().name())
                .deleted(part.getStatus() == PartStatus.DISCONTINUED)
                .groupId(part.getPartGroup().getId())
                .categoryId(part.getPartGroup().getCategory().getId())
                .standardCost(part.getStandardCost())
                .build();

        outboxService.saveEvent(
                "PART",
                part.getId(),
                "PartUpdated",
                part.getVersion(),
                payload
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
}
