package com.sampoom.backend.api.bom.service;

import com.sampoom.backend.api.bom.dto.BomDetailResponseDTO;
import com.sampoom.backend.api.bom.dto.BomRequestDTO;
import com.sampoom.backend.api.bom.dto.BomResponseDTO;
import com.sampoom.backend.api.bom.entity.Bom;
import com.sampoom.backend.api.bom.entity.BomMaterial;
import com.sampoom.backend.api.bom.repository.BomRepository;
import com.sampoom.backend.api.material.entity.Material;
import com.sampoom.backend.api.material.repository.MaterialRepository;
import com.sampoom.backend.api.part.entity.Part;
import com.sampoom.backend.api.part.repository.PartRepository;
import com.sampoom.backend.common.dto.PageResponseDTO;
import com.sampoom.backend.common.exception.NotFoundException;
import com.sampoom.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BomService {
    private final BomRepository bomRepository;
    private final PartRepository partRepository;
    private final MaterialRepository materialRepository;


    // BOM 생성
    @Transactional
    public BomResponseDTO createOrUpdateBom(BomRequestDTO requestDTO) {
        Part part = partRepository.findById(requestDTO.getPartId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.PART_NOT_FOUND));

        Bom bom = bomRepository.findByPart_Id(part.getId())
                .orElseGet(() -> Bom.builder()
                        .part(part)
                        .materials(new ArrayList<>())
                        .build());

        // 기존 자재를 Map으로 변환 (id 기준)
        Map<Long, BomMaterial> existingMaterials = bom.getMaterials().stream()
                .collect(Collectors.toMap(m -> m.getMaterial().getId(), m -> m));

        List<BomMaterial> newMaterialList = new ArrayList<>();

        for (BomRequestDTO.BomMaterialDTO dto : requestDTO.getMaterials()) {
            Material material = materialRepository.findById(dto.getMaterialId())
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.MATERIAL_NOT_FOUND));

            BomMaterial existing = existingMaterials.get(material.getId());

            if (existing != null) {
                // 이미 있는 자재 → 수량만 업데이트
                existing.updateQuantity(dto.getQuantity());
                newMaterialList.add(existing);
            } else {
                // 새로 추가된 자재
                BomMaterial newMat = BomMaterial.builder()
                        .bom(bom)
                        .material(material)
                        .quantity(dto.getQuantity())
                        .build();
                newMaterialList.add(newMat);
            }
        }

        // 기존에 있었는데 요청에서 빠진 자재는 제거
        bom.getMaterials().clear();
        bom.getMaterials().addAll(newMaterialList);

        bom.touchNow();

        return BomResponseDTO.from(bomRepository.save(bom));
    }


    // BOM 전체 목록 조회
    @Transactional(readOnly = true)
    public PageResponseDTO<BomResponseDTO> getBoms(int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
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

        bom.touchNow();

        return BomResponseDTO.from(bom);
    }

    // BOM 삭제
    @Transactional
    public void deleteBom(Long bomId) {
        Bom bom = bomRepository.findById(bomId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.BOM_NOT_FOUND));

        bomRepository.delete(bom);
    }

    // BOM 검색
    public PageResponseDTO<BomResponseDTO> searchBoms(String keyword, Long categoryId, Long groupId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Bom> bomPage = bomRepository.findByFilters(keyword, categoryId, groupId, pageable);

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
}
