package com.sampoom.backend.api.item.service;

import com.sampoom.backend.api.item.enums.ItemType;
import com.sampoom.backend.api.material.service.MaterialService;
import com.sampoom.backend.api.part.service.PartService;
import com.sampoom.backend.api.item.dto.ItemResponseDTO;
import com.sampoom.backend.common.dto.PageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemSearchService {

    private final MaterialService materialService;
    private final PartService partService;

    public PageResponseDTO<ItemResponseDTO> searchItems(String keyword, ItemType type, int page, int size) {

        List<ItemResponseDTO> allResults = new ArrayList<>();

        switch (type) {
            case MATERIAL ->  // 자재만
                materialService.searchMaterials(keyword, page, size)
                        .getContent()
                        .forEach(m -> allResults.add(ItemResponseDTO.ofMaterial(m)));

            case PART ->  // 부품만
                partService.searchParts(keyword, page, size)
                        .getContent()
                        .forEach(p -> allResults.add(ItemResponseDTO.ofPart(p)));

            case ALL -> {  // 전체
                materialService.searchMaterials(keyword, page, size)
                        .getContent()
                        .forEach(m -> allResults.add(ItemResponseDTO.ofMaterial(m)));
                partService.searchParts(keyword, page, size)
                        .getContent()
                        .forEach(p -> allResults.add(ItemResponseDTO.ofPart(p)));
            }
        }

        // 병합 결과 정렬 (코드순 or 이름순)
        allResults.sort(Comparator.comparing(ItemResponseDTO::getCode));

        // 페이지네이션 수동 처리
        int from = page * size;
        int to = Math.min(from + size, allResults.size());
        List<ItemResponseDTO> pageList = from < allResults.size() ? allResults.subList(from, to) : List.of();

        return PageResponseDTO.<ItemResponseDTO>builder()
                .content(pageList)
                .totalElements(allResults.size())
                .totalPages((int) Math.ceil((double) allResults.size() / size))
                .currentPage(page)
                .pageSize(size)
                .build();
    }
}
