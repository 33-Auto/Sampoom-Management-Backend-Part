package com.sampoom.backend.api.material.service;

import com.sampoom.backend.api.material.dto.MaterialCategoryResponseDTO;
import com.sampoom.backend.api.material.dto.MaterialRequestDTO;
import com.sampoom.backend.api.material.dto.MaterialResponseDTO;
import com.sampoom.backend.api.material.entity.Material;
import com.sampoom.backend.api.material.entity.MaterialCategory;
import com.sampoom.backend.api.material.repository.MaterialCategoryRepository;
import com.sampoom.backend.api.material.repository.MaterialRepository;
import com.sampoom.backend.common.dto.PageResponseDTO;
import com.sampoom.backend.common.exception.NotFoundException;
import com.sampoom.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialCategoryRepository categoryRepository;

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
                .build();

        materialRepository.save(material);

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
        material.updateBasicInfo(requestDTO.getName(), requestDTO.getMaterialUnit(),
                                requestDTO.getBaseQuantity(), requestDTO.getLeadTime());

        return new MaterialResponseDTO(material);
    }

    // 자재 삭제
    @Transactional
    public void deleteMaterial(Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.MATERIAL_NOT_FOUND));
        materialRepository.delete(material);
    }

    // 자재 검색
    @Transactional(readOnly = true)
    public PageResponseDTO<MaterialResponseDTO> searchMaterials(String keyword, int page, int size) {

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Material> materials = materialRepository.search(keyword, pageRequest);

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
