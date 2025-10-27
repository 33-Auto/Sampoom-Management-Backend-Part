package com.sampoom.backend.api.controller;

import com.sampoom.backend.api.dto.*;
import com.sampoom.backend.api.service.PartService;
import com.sampoom.backend.common.response.ApiResponse;
import com.sampoom.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Part", description = "부품 API")
@Slf4j
@RestController
@RequestMapping()
@RequiredArgsConstructor
public class PartController {

    private final PartService partService;

    @Operation(summary = "카테고리 목록 조회", description = "카테고리 목록 조회")
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> getCategories() {

        List<CategoryResponseDTO> categoryList = partService.findAllCategories();

        return ApiResponse.success(SuccessStatus.CATEGORY_LIST_SUCCESS, categoryList);
    }

    @Operation(summary = "그룹 목록 조회", description = "카테고리에 속한 그룹 목록 조회")
    @GetMapping("/categories/{categoryId}/groups")
    public ResponseEntity<ApiResponse<List<PartGroupResponseDTO>>> getGroups(@PathVariable Long categoryId) {

        List<PartGroupResponseDTO> groupList = partService.findGroupsByCategoryId(categoryId);

        return ApiResponse.success(SuccessStatus.GROUP_LIST_SUCCESS, groupList);
    }

    @Operation(summary = "부품 목록 조회", description = "특정 그룹에 속한 부품 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PartResponseDTO>>> getPartsByGroup(@RequestParam Long groupId) {

        List<PartResponseDTO> partList = partService.findPartsByGroupId(groupId);

        return ApiResponse.success(SuccessStatus.PART_LIST_SUCCESS, partList);
    }

    @Operation(summary = "부품 등록", description = "새로운 부품을 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<PartResponseDTO>> createPart(
            @Valid @RequestBody PartCreateRequestDTO partCreateRequestDTO
    ) {
        PartResponseDTO partResponse = partService.createPart(partCreateRequestDTO);

        return ApiResponse.success(SuccessStatus.PART_CREATE_SUCCESS, partResponse);
    }

    @Operation(summary = "부품 수정", description = "부품을 수정")
    @PutMapping("/{partId}")
    public ResponseEntity<ApiResponse<PartResponseDTO>> updatePart(
            @PathVariable Long partId,
            @Valid @RequestBody PartUpdateRequestDTO partUpdateRequestDTO
    ) {
        PartResponseDTO partResponse = partService.updatePart(partId, partUpdateRequestDTO);

        return ApiResponse.success(SuccessStatus.PART_UPDATE_SUCCESS, partResponse);
    }

    @Operation(summary = "부품 삭제", description = "부품 삭제")
    @DeleteMapping("/{partId}")
    public ResponseEntity<ApiResponse<Void>> deletePart(@PathVariable Long partId) {

        partService.deletePart(partId);

        return ApiResponse.success(SuccessStatus.PART_DELETE_SUCCESS, null);
    }

    @Operation(summary = "부품 검색", description = "부품 검색 (부품코드, 부품명)")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<PartResponseDTO>>> searchParts(@RequestParam String keyword) {

        List<PartResponseDTO> partList = partService.searchParts(keyword);

        return ApiResponse.success(SuccessStatus.PART_SEARCH_SUCCESS, partList);
    }

    @Operation(summary = "단일 부품 조회", description = "부품 ID로 부품 상세 정보를 조회합니다.")
    @GetMapping("/{partId}")
    public ResponseEntity<ApiResponse<PartResponseDTO>> getPartById(@PathVariable Long partId) {
        PartResponseDTO partResponse = partService.findPartById(partId);
        return ApiResponse.success(SuccessStatus.PART_DETAIL_SUCCESS, partResponse);
    }

}
