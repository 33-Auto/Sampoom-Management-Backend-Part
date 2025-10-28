package com.sampoom.backend.api.item.service;

import com.sampoom.backend.api.item.dto.ItemResponseDTO;
import com.sampoom.backend.api.item.enums.ItemType;
import com.sampoom.backend.api.material.service.MaterialService;
import com.sampoom.backend.api.part.service.PartService;
import com.sampoom.backend.common.dto.PageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemSearchService {

    private final MaterialService materialService;
    private final PartService partService;

    public PageResponseDTO<ItemResponseDTO> searchItems(String keyword, ItemType type, int page, int size) {

        List<ItemResponseDTO> mergedList = new ArrayList<>();

        switch (type) {
            case MATERIAL -> {
                var materials = materialService.searchMaterials(keyword, page, size);
                return PageResponseDTO.<ItemResponseDTO>builder()
                        .content(materials.getContent().stream()
                                .map(ItemResponseDTO::ofMaterial)
                                .toList())
                        .totalElements(materials.getTotalElements())
                        .totalPages(materials.getTotalPages())
                        .currentPage(materials.getCurrentPage())
                        .pageSize(materials.getPageSize())
                        .build();
            }

            case PART -> {
                var parts = partService.searchParts(keyword, page, size);
                return PageResponseDTO.<ItemResponseDTO>builder()
                        .content(parts.getContent().stream()
                                .map(ItemResponseDTO::ofPart)
                                .toList())
                        .totalElements(parts.getTotalElements())
                        .totalPages(parts.getTotalPages())
                        .currentPage(parts.getCurrentPage())
                        .pageSize(parts.getPageSize())
                        .build();
            }

            case ALL -> {
                var materials = materialService.searchMaterials(keyword, 0, Integer.MAX_VALUE);
                var parts = partService.searchParts(keyword, 0, Integer.MAX_VALUE);

                mergedList.addAll(materials.getContent().stream().map(ItemResponseDTO::ofMaterial).toList());
                mergedList.addAll(parts.getContent().stream().map(ItemResponseDTO::ofPart).toList());

                mergedList.sort(Comparator.comparing(ItemResponseDTO::getCode));

                int from = page * size;
                int to = Math.min(from + size, mergedList.size());
                List<ItemResponseDTO> pagedList = from < mergedList.size() ? mergedList.subList(from, to) : List.of();

                return PageResponseDTO.<ItemResponseDTO>builder()
                        .content(pagedList)
                        .totalElements(mergedList.size())
                        .totalPages((int) Math.ceil((double) mergedList.size() / size))
                        .currentPage(page)
                        .pageSize(size)
                        .build();
            }
        }
        throw new IllegalArgumentException("Invalid item type: " + type);
    }
}
