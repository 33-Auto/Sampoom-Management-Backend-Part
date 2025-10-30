package com.sampoom.backend.api.item.controller;

import com.sampoom.backend.api.item.dto.ItemResponseDTO;
import com.sampoom.backend.api.item.enums.ItemType;
import com.sampoom.backend.api.item.service.ItemService;
import com.sampoom.backend.common.dto.PageResponseDTO;
import com.sampoom.backend.common.response.ApiResponse;
import com.sampoom.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "ItemSearch", description = "부품/자재 통합 검색 API")
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemSearchController {

    private final ItemService itemService;

    @Operation(summary = "품목 통합 검색", description = """
            부품/자재/전체 품목을 검색합니다.
            - type: ALL / PART / MATERIAL
            - 부품일 때: partCategoryId, partGroupId 사용
            - 자재일 때: materialCategoryId 사용
            """)
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponseDTO<ItemResponseDTO>>> searchItems(
            @RequestParam(defaultValue = "ALL") ItemType type,
            @RequestParam(required = false) Long partCategoryId,
            @RequestParam(required = false) Long partGroupId,
            @RequestParam(required = false) Long materialCategoryId,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponseDTO<ItemResponseDTO> result =
                itemService.searchItems(type, partCategoryId, partGroupId, materialCategoryId, keyword, page, size);

        return ApiResponse.success(SuccessStatus.OK, result);
    }
}
