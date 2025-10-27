package com.sampoom.backend.api.part.service;

import com.sampoom.backend.api.part.dto.*;
import com.sampoom.backend.api.part.entity.PartCategory;
import com.sampoom.backend.api.part.entity.Part;
import com.sampoom.backend.api.part.entity.PartGroup;
import com.sampoom.backend.api.part.entity.PartStatus;
import com.sampoom.backend.api.part.repository.PartCategoryRepository;
import com.sampoom.backend.api.part.repository.PartGroupRepository;
import com.sampoom.backend.api.part.repository.PartRepository;
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
public class PartService {

    private final PartRepository partRepository;
    private final PartGroupRepository partGroupRepository;
    private final PartCategoryRepository categoryRepository;

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

        // 코드 자동 생성
        String nextCode = generateNextPartCode(partGroup.getId());

        Part newPart = new Part(nextCode, partCreateRequestDTO.getName(), partGroup);

        partRepository.save(newPart);

        return new PartListResponseDTO(newPart);
    }

    // 부품 수정
    @Transactional
    public PartListResponseDTO updatePart(Long partId, PartUpdateRequestDTO partUpdateRequestDTO) {
        // 수정할 부품을 조회
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.PART_NOT_FOUND.getMessage()));

        part.update(partUpdateRequestDTO);

        return new PartListResponseDTO(part);
    }

    // 부품 삭제
    @Transactional
    public void deletePart(Long partId) {

        // 삭제할 부품 조회
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.PART_NOT_FOUND));

        partRepository.delete(part);
    }

    // 부품 검색
    @Transactional
    public PageResponseDTO<PartListResponseDTO> searchParts(String keyword, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        Page<Part> parts = partRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCaseAndStatus(
                keyword, keyword, PartStatus.ACTIVE, pageRequest
        );

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

}
