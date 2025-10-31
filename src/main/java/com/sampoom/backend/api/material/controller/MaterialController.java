package com.sampoom.backend.api.material.controller;

import com.sampoom.backend.api.material.dto.MaterialCategoryResponseDTO;
import com.sampoom.backend.api.material.dto.MaterialRequestDTO;
import com.sampoom.backend.api.material.dto.MaterialResponseDTO;
import com.sampoom.backend.api.material.service.MaterialService;
import com.sampoom.backend.common.dto.PageResponseDTO;
import com.sampoom.backend.common.response.ApiResponse;
import com.sampoom.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Material", description = "Material 관련 API 입니다.")
@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @Operation(summary = "카테고리 조회", description = "모든 자재 카테고리를 조회합니다.")
    @GetMapping("/category")
    public ResponseEntity<ApiResponse<List<MaterialCategoryResponseDTO>>> getAllCategories() {
        return ApiResponse.success(SuccessStatus.OK, materialService.findAllCategories());
    }

    @Operation(summary = "카테고리별 자재 목록 조회", description = "특정 카테고리에 속한 자재 목록을 조회합니다.")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<PageResponseDTO<MaterialResponseDTO>>> getMaterialsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(SuccessStatus.OK, materialService.findMaterialsByCategory(categoryId, page, size));
    }

    @Operation(summary = "자재 목록 전체 조회", description = "모든 자재 정보를 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDTO<MaterialResponseDTO>>> getAllMaterials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(SuccessStatus.OK, materialService.findAllMaterials(page, size));
    }

    @Operation(summary = "자재 등록", description = "새로운 자재를 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<MaterialResponseDTO>> createMaterial(@Valid @RequestBody MaterialRequestDTO materialRequestDTO) {

        MaterialResponseDTO created = materialService.createMaterial(materialRequestDTO);

        return ApiResponse.success(SuccessStatus.CREATED,created);
    }

    @Operation(summary = "자재 수정", description = "기존 자재 정보를 수정합니다.")
    @PutMapping("/{materialId}")
    public ResponseEntity<ApiResponse<MaterialResponseDTO>> updateMaterial(
            @PathVariable("materialId") Long id,
            @Valid @RequestBody MaterialRequestDTO materialRequestDTO) {

        return ApiResponse.success(SuccessStatus.OK,materialService.updateMaterial(id, materialRequestDTO));
    }

    @Operation(summary = "자재 삭제", description = "자재를 삭제합니다.")
    @DeleteMapping("/{materialId}")
    public ResponseEntity<ApiResponse<Void>> deleteMaterial(@PathVariable("materialId") Long id) {
        materialService.deleteMaterial(id);
        return ApiResponse.success_only(SuccessStatus.OK);
    }

    @Operation(summary = "원자재 검색", description = "원자재명 또는 코드로 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponseDTO<MaterialResponseDTO>>> searchMaterials(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ApiResponse.success(
                SuccessStatus.OK,
                materialService.searchMaterials(keyword, categoryId, page, size)
        );
    }


//    @Operation(summary = "자재 검색", description = "자재 이름 또는 코드로 검색합니다.")
//    @GetMapping("/search")
//    public ResponseEntity<ApiResponse<PageResponseDTO<MaterialResponseDTO>>> searchMaterials(
//            @RequestParam(defaultValue = "") String keyword,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size
//    ) {
//        PageResponseDTO<MaterialResponseDTO> result = materialService.searchMaterials(keyword, page, size);
//
//        return ApiResponse.success(SuccessStatus.MATERIAL_LIST_SUCCESS, result);
//    }
}
