package com.sampoom.backend.api.material.service;

import com.sampoom.backend.api.material.dto.MaterialCategoryResponseDTO;
import com.sampoom.backend.api.material.dto.MaterialRequestDTO;
import com.sampoom.backend.api.material.dto.MaterialResponseDTO;
import com.sampoom.backend.api.material.entity.Material;
import com.sampoom.backend.api.material.entity.MaterialCategory;
import com.sampoom.backend.api.material.event.dto.MaterialEvent;
import com.sampoom.backend.api.material.repository.MaterialCategoryRepository;
import com.sampoom.backend.api.material.repository.MaterialRepository;
import com.sampoom.backend.api.part.event.service.OutboxService;
import com.sampoom.backend.common.dto.PageResponseDTO;
import com.sampoom.backend.common.exception.NotFoundException;
import com.sampoom.backend.common.response.ErrorStatus;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialCategoryRepository categoryRepository;
    private final OutboxService outboxService;

    // 카테고리 목록 조회
    @Transactional(readOnly = true)
    public List<MaterialCategoryResponseDTO> findAllCategories() {

        List<MaterialCategory> categories = categoryRepository.findAll();

        return categories.stream()
                .map(MaterialCategoryResponseDTO::new)
                .collect(Collectors.toList());
    }

    // 카테고리별 자재 목록 조회
    @Transactional
    public PageResponseDTO<MaterialResponseDTO> findMaterialsByCategory(Long categoryId, int page, int size) {

        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.CATEGORY_NOT_FOUND));

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Material> materials = materialRepository.findByMaterialCategoryId(categoryId, pageRequest);

        List<MaterialResponseDTO> dtoList = materials.stream()
                .map(MaterialResponseDTO::new)
                .collect(Collectors.toList());

        return PageResponseDTO.<MaterialResponseDTO>builder()
                .content(dtoList)
                .totalElements(materials.getTotalElements())
                .totalPages(materials.getTotalPages())
                .currentPage(page)
                .pageSize(size)
                .build();
    }

    // 전체 자재 목록 조회
    @Transactional(readOnly = true)
    public PageResponseDTO<MaterialResponseDTO> findAllMaterials(int page, int size) {

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Material> materials = materialRepository.findAll(pageRequest);

        List<MaterialResponseDTO> dtoList = materials.stream()
                .map(MaterialResponseDTO::new)
                .collect(Collectors.toList());

        return PageResponseDTO.<MaterialResponseDTO>builder()
                .content(dtoList)
                .totalElements(materials.getTotalElements())
                .totalPages(materials.getTotalPages())
                .currentPage(page)
                .pageSize(size)
                .build();
    }

    // 자재 생성
    @Transactional
    public MaterialResponseDTO createMaterial(MaterialRequestDTO requestDTO) {
        MaterialCategory category = categoryRepository.findById(requestDTO.getMaterialCategoryId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.CATEGORY_NOT_FOUND));

        String materialCode = generateNextMaterialCode(category.getId());

        Material material = Material.builder()
                .name(requestDTO.getName())
                .materialCode(materialCode)
                .materialUnit(requestDTO.getMaterialUnit())
                .baseQuantity(requestDTO.getBaseQuantity())
                .leadTime(requestDTO.getLeadTime())
                .materialCategory(category)
                .standardCost(Optional.ofNullable(requestDTO.getStandardCost()).orElse(0L))
                .build();

        materialRepository.save(material);

        // 이벤트 발행 - 부품 이벤트와 같은 방식으로 수정
        MaterialEvent.Payload payload = MaterialEvent.Payload.builder()
                .materialId(material.getId())
                .materialCode(material.getMaterialCode())
                .name(material.getName())
                .materialUnit(material.getMaterialUnit())
                .baseQuantity(material.getBaseQuantity())
                .leadTime(material.getLeadTime())
                .deleted(false)
                .materialCategoryId(category.getId())
                .build();

        MaterialEvent event = MaterialEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("MaterialCreated")
                .version(material.getVersion())
                .occurredAt(OffsetDateTime.now().toString())
                .payload(payload)
                .build();

        outboxService.saveEvent(
                "MATERIAL",
                material.getId(),
                "MaterialCreated",
                material.getVersion(),
                event.getPayload() // event 전체가 아닌 payload만 전달
        );

        return new MaterialResponseDTO(material);
    }

    // 자재 수정
    @Transactional
    public MaterialResponseDTO updateMaterial(Long id, MaterialRequestDTO requestDTO) {

        // 자재 조회
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.MATERIAL_NOT_FOUND));

        // 새 카테고리 조회
        MaterialCategory newCategory = categoryRepository.findById(requestDTO.getMaterialCategoryId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.CATEGORY_NOT_FOUND));

        // 카테고리가 변경 시 코드 재발급
        if (!material.getMaterialCategory().getId().equals(newCategory.getId())) {
            String nextCode = generateNextMaterialCode(newCategory.getId());
            material.changeCategory(newCategory, nextCode);
        }

        // 나머지 필드 수정
        material.updateBasicInfo(
                requestDTO.getName(),
                requestDTO.getMaterialUnit(),
                requestDTO.getBaseQuantity(),
                requestDTO.getLeadTime(),
                requestDTO.getStandardCost()
        );

        materialRepository.flush();


        // 이벤트 발행 - 부품 이벤트와 같은 방식으로 수정
        MaterialEvent.Payload payload = MaterialEvent.Payload.builder()
                .materialId(material.getId())
                .materialCode(material.getMaterialCode())
                .name(material.getName())
                .materialUnit(material.getMaterialUnit())
                .baseQuantity(material.getBaseQuantity())
                .leadTime(material.getLeadTime())
                .deleted(false)
                .materialCategoryId(material.getMaterialCategory().getId())
                .build();

        MaterialEvent event = MaterialEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("MaterialUpdated")
                .version(material.getVersion())
                .occurredAt(OffsetDateTime.now().toString())
                .payload(payload)
                .build();

        outboxService.saveEvent(
                "MATERIAL",
                material.getId(),
                "MaterialUpdated",
                material.getVersion(),
                event.getPayload() // event 전체가 아닌 payload만 전달
        );

        return new MaterialResponseDTO(material);
    }

    // 자재 삭제
    @Transactional
    public void deleteMaterial(Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.MATERIAL_NOT_FOUND));

        // 이벤트 발행 (삭제 전에) - 부품 이벤트와 같은 방식으로 수정
        MaterialEvent.Payload payload = MaterialEvent.Payload.builder()
                .materialId(material.getId())
                .materialCode(material.getMaterialCode())
                .name(material.getName())
                .materialUnit(material.getMaterialUnit())
                .baseQuantity(material.getBaseQuantity())
                .leadTime(material.getLeadTime())
                .deleted(true)
                .materialCategoryId(material.getMaterialCategory().getId())
                .build();

        MaterialEvent event = MaterialEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("MaterialDeleted")
                .version(material.getVersion())
                .occurredAt(OffsetDateTime.now().toString())
                .payload(payload)
                .build();

        outboxService.saveEvent(
                "MATERIAL",
                material.getId(),
                "MaterialDeleted",
                material.getVersion(),
                event.getPayload() // event 전체가 아닌 payload만 전달
        );

        materialRepository.delete(material);
    }

    // 자재 검색
    @Transactional(readOnly = true)
    public PageResponseDTO<MaterialResponseDTO> searchMaterials(
            String keyword,
            Long categoryId,
            int page,
            int size
    ) {
        PageRequest pageable = PageRequest.of(page, size);

        Page<Material> materials = materialRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // keyword
            if (keyword != null && !keyword.isBlank()) {
                Predicate nameLike = cb.like(root.get("name"), "%" + keyword + "%");
                Predicate codeLike = cb.like(root.get("materialCode"), "%" + keyword + "%");
                predicates.add(cb.or(nameLike, codeLike));
            }

            // 카테고리 필터
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("materialCategory").get("id"), categoryId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        List<MaterialResponseDTO> dtoList = materials.getContent().stream()
                .map(MaterialResponseDTO::new)
                .toList();

        return PageResponseDTO.<MaterialResponseDTO>builder()
                .content(dtoList)
                .totalElements(materials.getTotalElements())
                .totalPages(materials.getTotalPages())
                .currentPage(page)
                .pageSize(size)
                .build();
    }


    @Transactional(readOnly = true)
    public String generateNextMaterialCode(Long categoryId) {

        MaterialCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.CATEGORY_NOT_FOUND));

        // 최신 자재 찾기
        Material latest = materialRepository.findTopByMaterialCategoryIdOrderByIdDesc(categoryId);
        String latestCode = (latest != null) ? latest.getMaterialCode() : null;

        int nextSeq = 1;
        if (latest != null && latest.getMaterialCode() != null) {
            String[] parts = latest.getMaterialCode().split("-");
            if (parts.length >= 2) {
                try {
                    nextSeq = Integer.parseInt(parts[1]) + 1;
                } catch (NumberFormatException ignored) { }
            }
        }

        return String.format("%s-%04d", category.getCode(), nextSeq);
    }
}
