package com.sampoom.backend.api.service;

import com.sampoom.backend.api.domain.*;
import com.sampoom.backend.api.dto.*;
import com.sampoom.backend.common.exception.NotFoundException;
import com.sampoom.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartService {

    private final PartRepository partRepository;
    private final PartGroupRepository partGroupRepository;
    private final CategoryRepository categoryRepository;

    // 카테고리 목록 조회
    @Transactional
    public List<CategoryResponseDTO> findAllCategories() {

        List<Category> categories = categoryRepository.findAll();

        return categories.stream()
                .map(CategoryResponseDTO::new)
                .collect(Collectors.toList());
    }

    // 카테고리에 속한 그룹 목록 조회
    @Transactional
    public List<PartGroupResponseDTO> findGroupsByCategoryId(Long categoryId) {

        List<PartGroup> partGroups = partGroupRepository.findByCategoryId(categoryId);

        return partGroups.stream()
                .map(PartGroupResponseDTO::new)
                .collect(Collectors.toList());
    }


    // 특정 그룹에 속한 부품 목록 조회
    @Transactional
    public List<PartResponseDTO> findPartsByGroupId(Long groupId) {

        List<Part> parts = partRepository.findByPartGroupIdAndStatus(groupId, PartStatus.ACTIVE);

        return parts.stream()
                .map(PartResponseDTO::new)
                .collect(Collectors.toList());
    }

    // 신규 부품 생성
    @Transactional
    public PartResponseDTO createPart(PartCreateRequestDTO partCreateRequestDTO) {

        // DTO에 담겨온 groupId로 PartGroup 엔티티 조회
        PartGroup partGroup = partGroupRepository.findById(partCreateRequestDTO.getGroupId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.GROUP_NOT_FOUND.getMessage()));

        Part newPart = Part.create(partCreateRequestDTO, partGroup);
        partRepository.save(newPart);

        return new PartResponseDTO(newPart);
    }

    // 부품 수정
    @Transactional
    public PartResponseDTO updatePart(Long partId, PartUpdateRequestDTO partUpdateRequestDTO) {
        // 수정할 부품을 조회
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.PART_NOT_FOUND.getMessage()));

        part.update(partUpdateRequestDTO);

        return new PartResponseDTO(part);
    }

    // 부품 삭제
    @Transactional
    public void deletePart(Long partId) {

        // 삭제할 부품 조회
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.PART_NOT_FOUND.getMessage()));

        part.delete();
    }

    // 부품 검색
    @Transactional
    public List<PartResponseDTO> searchParts(String keyword) {

        List<Part> parts = partRepository.searchByKeyword(keyword);

        return  parts.stream()
                .map(PartResponseDTO::new)
                .collect(Collectors.toList());
    }
}
