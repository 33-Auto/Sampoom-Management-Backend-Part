package com.sampoom.backend.api.bom.controller;

import com.sampoom.backend.api.bom.dto.BomDetailResponseDTO;
import com.sampoom.backend.api.bom.dto.BomRequestDTO;
import com.sampoom.backend.api.bom.dto.BomResponseDTO;
import com.sampoom.backend.api.bom.service.BomService;
import com.sampoom.backend.common.dto.PageResponseDTO;
import com.sampoom.backend.common.response.ApiResponse;
import com.sampoom.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "BOM", description = "BOM API")
@RestController
@RequestMapping("/api/boms")
@RequiredArgsConstructor
public class BomController {

    private final BomService bomService;

    @Operation(summary = "BOM 추가", description = "새로운 BOM을 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<BomResponseDTO>> createOrUpdateBom(@RequestBody BomRequestDTO bomRequestDTO) {
        return ApiResponse.success(SuccessStatus.CREATED, bomService.createOrUpdateBom(bomRequestDTO));
    }

    @Operation(summary = "BOM 목록 조회", description = "페이징 처리된 BOM 목록을 조회하고, 카테고리나 그룹으로 필터링합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDTO<BomResponseDTO>>> getBoms(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(SuccessStatus.OK, bomService.searchBoms(null, categoryId, groupId, page, size));
    }

    @Operation(summary = "BOM 상세 조회", description = "특정 BOM의 상세 정보를 조회합니다.")
    @GetMapping("/{bomId}")
    public ResponseEntity<ApiResponse<BomDetailResponseDTO>> getBomDetail(@PathVariable Long bomId) {
        return ApiResponse.success(SuccessStatus.OK, bomService.getBomDetail(bomId));
    }

    @Operation(summary = "BOM 수정", description = "특정 BOM 정보를 수정합니다.")
    @PutMapping("/{bomId}")
    public ResponseEntity<ApiResponse<BomResponseDTO>> updateBom(
            @PathVariable Long bomId,
            @RequestBody BomRequestDTO bomRequestDTO) {
        return ApiResponse.success(SuccessStatus.OK, bomService.updateBom(bomId, bomRequestDTO));
    }

    @Operation(summary = "BOM 삭제", description = "특정 BOM을 삭제합니다.")
    @DeleteMapping("/{bomId}")
    public ResponseEntity<ApiResponse<Void>> deleteBom(@PathVariable Long bomId) {
        bomService.deleteBom(bomId);
        return ApiResponse.success_only(SuccessStatus.OK);
    }

    @Operation(summary = "BOM 검색", description = "부품 이름 또는 부품 코드로 BOM을 검색하고, 카테고리나 그룹으로 필터링합니다.")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponseDTO<BomResponseDTO>>> searchBoms(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(SuccessStatus.OK,
                bomService.searchBoms(keyword, categoryId, groupId, page, size));
    }
}
