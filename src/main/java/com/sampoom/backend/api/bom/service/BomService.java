package com.sampoom.backend.api.bom.service;

import com.sampoom.backend.api.bom.dto.BomDetailResponseDTO;
import com.sampoom.backend.api.bom.dto.BomRequestDTO;
import com.sampoom.backend.api.bom.dto.BomResponseDTO;
import com.sampoom.backend.api.bom.entity.Bom;
import com.sampoom.backend.api.bom.entity.BomComplexity;
import com.sampoom.backend.api.bom.entity.BomMaterial;
import com.sampoom.backend.api.bom.entity.BomStatus;
import com.sampoom.backend.api.bom.event.dto.BomEvent;
import com.sampoom.backend.api.bom.repository.BomRepository;
import com.sampoom.backend.api.material.entity.Material;
import com.sampoom.backend.api.material.repository.MaterialRepository;
import com.sampoom.backend.api.part.entity.Part;
import com.sampoom.backend.common.outbox.service.OutboxService;
import com.sampoom.backend.api.part.repository.PartRepository;
import com.sampoom.backend.api.part.service.PartService;
import com.sampoom.backend.common.dto.PageResponseDTO;
import com.sampoom.backend.common.exception.BadRequestException;
import com.sampoom.backend.common.exception.NotFoundException;
import com.sampoom.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BomService {
    private final BomRepository bomRepository;
    private final PartRepository partRepository;
    private final MaterialRepository materialRepository;
    private final OutboxService outboxService;
    private final PartService partService;


    // BOM 생성
    @Transactional
    public BomResponseDTO createBom(BomRequestDTO requestDTO) {

        // 부품 존재 여부 확인
        Part part = partRepository.findById(requestDTO.getPartId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.PART_NOT_FOUND));

        // 기존 BOM 존재 시 등록 불가
        if (bomRepository.existsByPart_Id(part.getId())) {
            throw new BadRequestException(ErrorStatus.DUPLICATE_BOM);
        }

        // 요청 자재 중복 제거 및 수량 합산
        Map<Long, Double> idToQty = requestDTO.getMaterials().stream()
                .collect(Collectors.toMap(
                        BomRequestDTO.BomMaterialDTO::getMaterialId,
                        BomRequestDTO.BomMaterialDTO::getQuantity,
                        Double::sum // 중복 materialId 수량 합산
                ));

        // 한 번의 쿼리로 모든 자재 조회 (N+1 방지)
        List<Material> materials = materialRepository.findAllById(idToQty.keySet());

        if (materials.size() != idToQty.size()) {
            throw new NotFoundException(ErrorStatus.MATERIAL_NOT_FOUND);
        }

        // BOM 자재 리스트 구성
//        List<BomMaterial> newMaterialList = new ArrayList<>();
//
//        for (Map.Entry<Long, Long> entry : idToQty.entrySet()) {
//            Long materialId = entry.getKey();
//            Long quantity = entry.getValue();
//            Material material = matMap.get(materialId);
//
//            BomMaterial existing = existingMaterials.get(materialId);
//            if (existing != null) {
//                existing.updateQuantity(quantity);
//                newMaterialList.add(existing);
//            } else {
//                BomMaterial newMat = BomMaterial.builder()
//                        .bom(bom)
//                        .material(material)
//                        .quantity(quantity)
//                        .build();
//                newMaterialList.add(newMat);
//            }
//        }

        // 4️⃣ BOM 생성
        Bom bom = Bom.builder()
                .part(part)
                .materials(new ArrayList<>())
                .status(
                        requestDTO.getBomStatus() != null
                                ? requestDTO.getBomStatus()
                                : BomStatus.PENDING_APPROVAL
                )
                .complexity(BomComplexity.SIMPLE)
                .build();

        // 5️⃣ 자재 매핑
        for (Material material : materials) {
            Double quantity = idToQty.get(material.getId());
            BomMaterial bm = BomMaterial.builder()
                    .bom(bom)
                    .material(material)
                    .quantity(quantity)
                    .build();
            bom.addMaterial(bm);
        }

        // 6️⃣ 계산 로직
        bom.calculateComplexity();
        bom.calculateTotalCost();

        // 수정일 갱신 후 저장
        bom.touchNow();

        Bom saved =  bomRepository.saveAndFlush(bom);
        saved.generateBomCode(saved.getId());
        bomRepository.save(saved);

        // 코드 생성 (id 기반)
        if (saved.getBomCode() == null) {
            saved.generateBomCode(saved.getId());
            bomRepository.save(saved); // 다시 저장해서 DB 반영
        }

        // 부품 원가 갱신
//        part.updateStandardCost(bom.getTotalCost());
//        partRepository.save(part);

        // 8️⃣ 이벤트 발행
        publishBomEvent(saved, "BomCreated");

        // Part 표준 비용 업데이트 (BOM 비용 + Process 비용)
        partService.updateStandardCostFromBomAndProcess(saved.getPart().getId());

        return BomResponseDTO.from(saved);
    }


    // BOM 전체 목록 조회
    @Transactional(readOnly = true)
    public PageResponseDTO<BomResponseDTO> getBoms(int page, int size) {

        // BOM 코드 기준 오름차순 정렬
        Pageable pageable = PageRequest.of(page, size, Sort.by("bomCode").ascending());
        Page<Bom> bomPage = bomRepository.findAll(pageable);

        return PageResponseDTO.<BomResponseDTO>builder()
                .content(bomPage.getContent().stream()
                        .map(BomResponseDTO::from)
                        .collect(Collectors.toList()))
                .totalElements(bomPage.getTotalElements())
                .totalPages(bomPage.getTotalPages())
                .currentPage(bomPage.getNumber())
                .pageSize(bomPage.getSize())
                .build();
    }


    // BOM 상세 조회
    @Transactional(readOnly = true)
    public BomDetailResponseDTO getBomDetail(Long bomId) {
        Bom bom = bomRepository.findById(bomId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.BOM_NOT_FOUND));

        return BomDetailResponseDTO.from(bom);
    }


    // BOM 수정
    @Transactional
    public BomResponseDTO updateBom(Long bomId, BomRequestDTO bomRequestDTO) {
        Bom bom = bomRepository.findById(bomId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.BOM_NOT_FOUND));

        // 기존 자재 삭제
        bom.getMaterials().clear();

        // 새 자재 추가
        for (BomRequestDTO.BomMaterialDTO materialDTO : bomRequestDTO.getMaterials()) {
            Material material = materialRepository.findById(materialDTO.getMaterialId())
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.MATERIAL_NOT_FOUND));

            BomMaterial bomMaterial = BomMaterial.builder()
                    .bom(bom)
                    .material(material)
                    .quantity(materialDTO.getQuantity())
                    .build();

            bom.addMaterial(bomMaterial);
        }


        // 복잡도 재계산
        bom.calculateComplexity();
        bom.calculateTotalCost();

        bom.touchNow();

        Bom saved = bomRepository.saveAndFlush(bom);

//        Part part = bom.getPart();
//        part.updateStandardCost(bom.getTotalCost());
//        partRepository.save(part);

        publishBomEvent(saved, "BomUpdated");

        // Part 표준 비용 업데이트 (BOM 비용 + Process 비용)
        partService.updateStandardCostFromBomAndProcess(saved.getPart().getId());

        return BomResponseDTO.from(saved);
    }

    // BOM 삭제
    @Transactional
    public void deleteBom(Long bomId) {
        Bom bom = bomRepository.findById(bomId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.BOM_NOT_FOUND));

        Long partId = bom.getPart().getId(); // Part ID 백업

        bomRepository.delete(bom);

        publishBomDeletedEvent(bom);

        // Part 표준 비용 업데이트 (BOM 삭제로 인한 비용 재계산)
        partService.updateStandardCostFromBomAndProcess(partId);
    }


    /** ----------------------------
     * Outbox 이벤트 발행 메서드
     * ---------------------------- */
    private void publishBomEvent(Bom bom, String eventType) {
        BomEvent.Payload payload = BomEvent.Payload.builder()
                .bomId(bom.getId())
                .partId(bom.getPart().getId())
                .partCode(bom.getPart().getCode())
                .partName(bom.getPart().getName())
                .status(bom.getStatus().name())
                .complexity(bom.getComplexity().name())
                .deleted(false)
                .totalCost(bom.getTotalCost() != null ? bom.getTotalCost().doubleValue() : 0.0)
                .materials(bom.getMaterials().stream()
                        .map(m -> BomEvent.Payload.MaterialInfo.builder()
                                .materialId(m.getMaterial().getId())
                                .materialName(m.getMaterial().getName())
                                .materialCode(m.getMaterial().getMaterialCode())
                                .unit(m.getMaterial().getMaterialUnit())
                                .quantity(m.getQuantity())
                                .build())
                        .toList())
                .build();

        BomEvent event = BomEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .version(bom.getVersion())
                .occurredAt(OffsetDateTime.now().toString())
                .payload(payload)
                .build();

        outboxService.saveEvent(
                "BOM",
                bom.getId(),
                eventType,
                bom.getVersion(),
                event.getPayload()
        );
    }

    private void publishBomDeletedEvent(Bom bom) {
        BomEvent.Payload payload = BomEvent.Payload.builder()
                .bomId(bom.getId())
                .partId(bom.getPart().getId())
                .partCode(bom.getPart().getCode())
                .partName(bom.getPart().getName())
                .status("DELETED")
                .complexity(bom.getComplexity().name())
                .deleted(true)
                .totalCost(0.0)
                .materials(Collections.emptyList())
                .build();

        BomEvent event = BomEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("BomDeleted")
                .version(bom.getVersion())
                .occurredAt(OffsetDateTime.now().toString())
                .payload(payload)
                .build();

        outboxService.saveEvent(
                "BOM",
                bom.getId(),
                "BomDeleted",
                bom.getVersion(),
                event.getPayload()
        );
    }



    // BOM 검색
    public PageResponseDTO<BomResponseDTO> searchBoms(
            String keyword,
            Long categoryId,
            Long groupId,
            BomStatus status,
            BomComplexity complexity,
            int page,
            int size
    ) {
        // BOM 코드 기준 오름차순 정렬
        Pageable pageable = PageRequest.of(page, size, Sort.by("bomCode").ascending());
        Page<Bom> bomPage = bomRepository.findByFilters(
                keyword,
                categoryId,
                groupId,
                status,
                complexity,
                pageable
        );

        return PageResponseDTO.<BomResponseDTO>builder()
                .content(bomPage.getContent().stream()
                        .map(BomResponseDTO::from)
                        .collect(Collectors.toList()))
                .totalElements(bomPage.getTotalElements())
                .totalPages(bomPage.getTotalPages())
                .currentPage(bomPage.getNumber())
                .pageSize(bomPage.getSize())
                .build();
    }

    /**
     * BOM 상태 변경 (활성/비활성 등)
     */
    @Transactional
    public void updateBomStatus(Long bomId, BomStatus newStatus) {
        Bom bom = bomRepository.findById(bomId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.BOM_NOT_FOUND));
        bom.updateStatus(newStatus);
        bomRepository.save(bom);
    }

    /**
     * 모든 BOM의 totalCost 재계산 (quantity가 Double로 변경된 후 실행)
     */
    @Transactional
    public void recalculateAllBomTotalCosts() {
        List<Bom> boms = bomRepository.findAll();
        for (Bom bom : boms) {
            bom.calculateTotalCost();
        }
        bomRepository.saveAll(boms);
    }

    /**
     * 특정 BOM의 totalCost 재계산
     */
    @Transactional
    public void recalculateBomTotalCost(Long bomId) {
        Bom bom = bomRepository.findById(bomId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.BOM_NOT_FOUND));
        bom.calculateTotalCost();
        bomRepository.save(bom);
    }
}
