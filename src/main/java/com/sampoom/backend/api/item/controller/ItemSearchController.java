package com.sampoom.backend.api.item.controller;

import com.sampoom.backend.api.item.dto.ItemResponseDTO;
import com.sampoom.backend.api.item.enums.ItemType;
import com.sampoom.backend.api.item.service.ItemSearchService;
import com.sampoom.backend.common.dto.PageResponseDTO;
import com.sampoom.backend.common.response.ApiResponse;
import com.sampoom.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Item", description = "통합 검색 API (자재 + 부품)")
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemSearchController {

    private final ItemSearchService itemSearchService;

    @Operation(summary = "통합 검색", description = "자재, 부품, 또는 전체 항목을 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponseDTO<ItemResponseDTO>>> searchItems(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "ALL") ItemType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponseDTO<ItemResponseDTO> result = itemSearchService.searchItems(keyword, type, page, size);
        return ApiResponse.success(SuccessStatus.ITEM_LIST_SUCCESS, result);
    }
}
