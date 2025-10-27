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

@Tag(name = "Item", description = "자재/부품 통합 검색 API")
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemSearchController {

    private final ItemSearchService itemSearchService;

    @Operation(summary = "통합 검색", description = "자재와 부품을 품목명 또는 코드 기준으로 통합 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponseDTO<ItemResponseDTO>>> searchItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "ALL") ItemType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponseDTO<ItemResponseDTO> result = itemSearchService.searchItems(keyword, type, page, size);

        return ApiResponse.success(SuccessStatus.OK, result);
    }
}
