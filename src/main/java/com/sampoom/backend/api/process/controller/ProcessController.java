package com.sampoom.backend.api.process.controller;

import com.sampoom.backend.api.process.dto.*;
import com.sampoom.backend.api.process.entity.ProcessStatus;
import com.sampoom.backend.api.process.service.ProcessService;
import com.sampoom.backend.common.response.ApiResponse;
import com.sampoom.backend.common.response.PageResponseDto;
import com.sampoom.backend.common.response.PageResponseDto;
import com.sampoom.backend.common.response.PageResponseDto;
import com.sampoom.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Process", description = "공정 API")
@RestController
@RequestMapping("/processes")
@RequiredArgsConstructor
public class ProcessController {

    private final ProcessService processService;

    @Operation(summary = "공정 등록", description = "부품, 버전, 상태 및 공정 순서(여러 개)를 등록합니다.")
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // 관리자만 수정 가능
    public ResponseEntity<ApiResponse<ProcessResponseDTO>> create(@Valid @RequestBody ProcessCreateRequestDTO request) {
        ProcessResponseDTO response = processService.create(request);
        return ApiResponse.success(SuccessStatus.CREATED, response);
    }


    @Operation(summary = "공정 검색/목록 조회", description = "공정명 또는 공정 코드로 검색하거나, 상태별로 필터링하여 공정 목록을 페이징 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<ProcessResponseDTO>>> search(
            @RequestParam(value = "query", required = false) String q,
            @RequestParam(value = "status", required = false) ProcessStatus status,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "groupId", required = false) Long groupId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        PageResponseDto<ProcessResponseDTO> response = processService.search(q, status, categoryId, groupId, page, size);
        return ApiResponse.success(SuccessStatus.OK, response);
    }


    @Operation(summary = "공정 상세 조회", description = "공정 ID로 공정의 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcessResponseDTO>> get(@PathVariable Long id) {
        ProcessResponseDTO response = processService.get(id);
        return ApiResponse.success(SuccessStatus.OK, response);
    }

    @Operation(summary = "공정 수정",description = "부품(partId) 변경 및 공정 스텝(전량 교체), 버전, 상태를 수정합니다.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // 관리자만 수정 가능
    public ResponseEntity<ApiResponse<ProcessResponseDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProcessUpdateRequestDTO request
    ) {
        ProcessResponseDTO response = processService.update(id, request);
        return ApiResponse.success(SuccessStatus.OK, response);
    }

    @Operation(summary = "공정 삭제", description = "공정을 삭제합니다.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // 관리자만 수정 가능
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        processService.delete(id);
        return ApiResponse.success_only(SuccessStatus.OK);
    }
}
