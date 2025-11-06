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
        // 디버깅용 로그
        System.out.println("=== ItemService.searchItems ===");
        System.out.println("itemType: " + itemType);
        System.out.println("partCategoryId: " + partCategoryId);
        System.out.println("partGroupId: " + partGroupId);
        System.out.println("materialCategoryId: " + materialCategoryId);
        System.out.println("keyword: " + keyword);

        return switch (itemType) {
            case PART -> convertParts(partService.searchParts(keyword, partCategoryId, partGroupId, page, size));
            case MATERIAL -> {
                System.out.println("MATERIAL 검색 - materialCategoryId: " + materialCategoryId + " 전달");
                yield convertMaterials(materialService.searchMaterials(keyword, materialCategoryId, page, size));
            }
            case ALL -> searchAllItems(keyword, partCategoryId, partGroupId, materialCategoryId, page, size);
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

    // ALL 타입 검색
    private PageResponseDTO<ItemResponseDTO> searchAllItems(
            String keyword,
            Long partCategoryId,
            Long partGroupId,
            Long materialCategoryId,
            int page,
            int size
    ) {
        // 전체 데이터를 한 번에 가져와서 병합 후 페이지네이션

        // 부품 데이터를 모두 가져오기 (부품 관련 필터만 적용)
        PageResponseDTO<PartListResponseDTO> allParts = partService.searchParts(keyword, partCategoryId, partGroupId, 0, Integer.MAX_VALUE);
        List<ItemResponseDTO> partItems = allParts.getContent().stream()
                .map(ItemResponseDTO::ofPart)
                .toList();

        // 자재 데이터를 모두 가져오기 (자재 관련 필터만 적용)
        PageResponseDTO<MaterialResponseDTO> allMaterials = materialService.searchMaterials(keyword, materialCategoryId, 0, Integer.MAX_VALUE);
        List<ItemResponseDTO> materialItems = allMaterials.getContent().stream()
                .map(ItemResponseDTO::ofMaterial)
                .toList();

        // 전체 데이터 병합
        List<ItemResponseDTO> allItems = new ArrayList<>();
        allItems.addAll(partItems);
        allItems.addAll(materialItems);

        // 전체 개수
        long totalElements = allItems.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // 현재 페이지에 해당하는 데이터 추출
        int start = page * size;
        int end = Math.min(start + size, allItems.size());
        List<ItemResponseDTO> pageContent = start < allItems.size() ?
                allItems.subList(start, end) : new ArrayList<>();

        return PageResponseDTO.<ItemResponseDTO>builder()
                .content(pageContent)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .currentPage(page)
                .pageSize(size)
                .build();
    }
}
