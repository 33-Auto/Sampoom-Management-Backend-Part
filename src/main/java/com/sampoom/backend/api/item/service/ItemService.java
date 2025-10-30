package com.sampoom.backend.api.item.service;

import com.sampoom.backend.api.item.dto.ItemResponseDTO;
import com.sampoom.backend.api.item.enums.ItemType;
import com.sampoom.backend.api.material.dto.MaterialResponseDTO;
import com.sampoom.backend.api.material.service.MaterialService;
import com.sampoom.backend.api.part.dto.PartListResponseDTO;
import com.sampoom.backend.api.part.service.PartService;
import com.sampoom.backend.common.dto.PageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final MaterialService materialService;
    private final PartService partService;

    // 품목 통합 검색
    public PageResponseDTO<ItemResponseDTO> searchItems(
            ItemType itemType,
            Long partCategoryId,
            Long partGroupId,
            Long materialCategoryId,
            String keyword,
            int page,
            int size
    ) {
        return switch (itemType) {
            case PART -> convertParts(partService.searchParts(keyword, partCategoryId, partGroupId, page, size));
            case MATERIAL -> convertMaterials(materialService.searchMaterials(keyword, materialCategoryId, page, size));
            case ALL -> mergeResults(
                    convertParts(partService.searchParts(keyword, partCategoryId, partGroupId, page, size)),
                    convertMaterials(materialService.searchMaterials(keyword, materialCategoryId, page, size))
            );
        };
    }

    private PageResponseDTO<ItemResponseDTO> convertParts(PageResponseDTO<PartListResponseDTO> dtoPage) {
        List<ItemResponseDTO> content = dtoPage.getContent().stream()
                .map(ItemResponseDTO::ofPart)
                .toList();

        return PageResponseDTO.<ItemResponseDTO>builder()
                .content(content)
                .totalElements(dtoPage.getTotalElements())
                .totalPages(dtoPage.getTotalPages())
                .currentPage(dtoPage.getCurrentPage())
                .pageSize(dtoPage.getPageSize())
                .build();
    }

    private PageResponseDTO<ItemResponseDTO> convertMaterials(PageResponseDTO<MaterialResponseDTO> dtoPage) {
        List<ItemResponseDTO> content = dtoPage.getContent().stream()
                .map(ItemResponseDTO::ofMaterial)
                .toList();

        return PageResponseDTO.<ItemResponseDTO>builder()
                .content(content)
                .totalElements(dtoPage.getTotalElements())
                .totalPages(dtoPage.getTotalPages())
                .currentPage(dtoPage.getCurrentPage())
                .pageSize(dtoPage.getPageSize())
                .build();
    }

    private PageResponseDTO<ItemResponseDTO> mergeResults(
            PageResponseDTO<ItemResponseDTO> partPage,
            PageResponseDTO<ItemResponseDTO> materialPage
    ) {
        List<ItemResponseDTO> merged = new ArrayList<>();
        merged.addAll(partPage.getContent());
        merged.addAll(materialPage.getContent());

        return PageResponseDTO.<ItemResponseDTO>builder()
                .content(merged)
                .totalElements(partPage.getTotalElements() + materialPage.getTotalElements())
                .totalPages(Math.max(partPage.getTotalPages(), materialPage.getTotalPages()))
                .currentPage(partPage.getCurrentPage())
                .pageSize(partPage.getPageSize())
                .build();
    }
}
