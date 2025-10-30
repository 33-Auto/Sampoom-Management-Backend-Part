package com.sampoom.backend.api.part.controller;

import com.sampoom.backend.api.part.dto.*;
import com.sampoom.backend.api.part.service.PartService;
import com.sampoom.backend.common.dto.PageResponseDTO;
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
@RequestMapping("/api/parts")
@RequiredArgsConstructor
public class PartController {

    private final PartService partService;

    @Operation(summary = "카테고리 목록 조회", description = "카테고리 목록 조회")
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<PartCategoryResponseDTO>>> getCategories() {

        List<PartCategoryResponseDTO> categoryList = partService.findAllCategories();

        return ApiResponse.success(SuccessStatus.CATEGORY_LIST_SUCCESS, categoryList);
    }

    @Operation(summary = "카테고리별 그룹 목록 조회", description = "카테고리에 속한 그룹 목록 조회")
    @GetMapping("/categories/{categoryId}/groups")
    public ResponseEntity<ApiResponse<List<PartGroupResponseDTO>>> getGroups(@PathVariable Long categoryId) {

        List<PartGroupResponseDTO> groupList = partService.findGroupsByCategoryId(categoryId);

        return ApiResponse.success(SuccessStatus.GROUP_LIST_SUCCESS, groupList);
    }

    @Operation(summary = "카테고리별 부품 목록 조회 (그룹 안 정했을 때)", description = "카테고리 내 모든 그룹의 부품을 한 번에 조회합니다.")
    @GetMapping("/categories/{categoryId}/parts")
    public ResponseEntity<ApiResponse<PageResponseDTO<PartListResponseDTO>>> getPartsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponseDTO<PartListResponseDTO> result = partService.findAllPartsByCategory(categoryId, page, size);

        return ApiResponse.success(SuccessStatus.PART_LIST_SUCCESS, result);
    }

    @Operation(summary = "그룹별 부품 목록 조회", description = "특정 그룹에 속한 부품 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDTO<PartListResponseDTO>>> getPartsByGroup(
            @RequestParam Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponseDTO<PartListResponseDTO> partList = partService.findPartsByGroup(groupId, page, size);

        return ApiResponse.success(SuccessStatus.PART_LIST_SUCCESS, partList);
    }

    @Operation(summary = "부품 등록", description = "새로운 부품을 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<PartListResponseDTO>> createPart(
            @Valid @RequestBody PartCreateRequestDTO partCreateRequestDTO
    ) {
        PartListResponseDTO partResponse = partService.createPart(partCreateRequestDTO);

        return ApiResponse.success(SuccessStatus.PART_CREATE_SUCCESS, partResponse);
    }

    @Operation(summary = "부품 수정", description = "부품을 수정")
    @PutMapping("/{partId}")
    public ResponseEntity<ApiResponse<PartListResponseDTO>> updatePart(
            @PathVariable Long partId,
            @Valid @RequestBody PartUpdateRequestDTO partUpdateRequestDTO
    ) {
        PartListResponseDTO partResponse = partService.updatePart(partId, partUpdateRequestDTO);

        return ApiResponse.success(SuccessStatus.PART_UPDATE_SUCCESS, partResponse);
    }

    @Operation(summary = "부품 삭제", description = "부품 삭제")
    @DeleteMapping("/{partId}")
    public ResponseEntity<ApiResponse<Void>> deletePart(@PathVariable Long partId) {

        partService.deletePart(partId);

        return ApiResponse.success(SuccessStatus.PART_DELETE_SUCCESS, null);
    }

//    @Operation(summary = "부품 검색", description = "부품 이름 또는 코드로 검색")
//    @GetMapping("/search")
//    public ResponseEntity<ApiResponse<PageResponseDTO<PartListResponseDTO>>> searchParts(
//            @RequestParam(defaultValue = "") String keyword,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size
//    ) {
//        PageResponseDTO<PartListResponseDTO> partPage = partService.searchParts(keyword, page, size);
//
//        return ApiResponse.success(SuccessStatus.PART_LIST_SUCCESS, partPage);
//    }
}
