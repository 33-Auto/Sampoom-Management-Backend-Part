package com.sampoom.backend.api.workcenter.controller;

import com.sampoom.backend.api.workcenter.dto.WorkCenterCreateRequestDTO;
import com.sampoom.backend.api.workcenter.dto.WorkCenterResponseDTO;
import com.sampoom.backend.api.workcenter.dto.WorkCenterUpdateRequestDTO;
import com.sampoom.backend.api.workcenter.entity.WorkCenterStatus;
import com.sampoom.backend.api.workcenter.entity.WorkCenterType;
import com.sampoom.backend.api.workcenter.service.WorkCenterService;
import com.sampoom.backend.common.response.ApiResponse;
import com.sampoom.backend.common.response.PageResponseDto;
import com.sampoom.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "WorkCenter", description = "작업장 API")
@RestController
@RequestMapping("/work-centers")
@RequiredArgsConstructor
public class WorkCenterController {

    private final WorkCenterService workCenterService;

    @Operation(summary = "작업장 등록", description = "작업장명, 유형, 상태, 일일 가동시간, 효율성, 시간당비용을 입력하여 작업장을 등록합니다.")
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // 관리자만 수정 가능
    public ResponseEntity<ApiResponse<WorkCenterResponseDTO>> create(@Valid @RequestBody WorkCenterCreateRequestDTO request) {
        WorkCenterResponseDTO response = workCenterService.create(request);
        return ApiResponse.success(SuccessStatus.WORKCENTER_CREATE_SUCCESS, response);
    }

    @Operation(summary = "작업장 목록 조회", description = "검색(q: 이름 부분일치), 필터(type: INTERNAL/EXTERNAL, status: ACTIVE/INACTIVE/MAINTENANCE)")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<WorkCenterResponseDTO>>> list(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "type", required = false) WorkCenterType type,
            @RequestParam(name = "status", required = false) WorkCenterStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponseDto<WorkCenterResponseDTO> body = workCenterService.search(query, type, status, page, size);
        return ApiResponse.success(SuccessStatus.WORKCENTER_LIST_SUCCESS, body);
    }

    @Operation(summary = "작업장 상세 조회", description = "ID로 작업장 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkCenterResponseDTO>> get(@PathVariable Long id) {
        WorkCenterResponseDTO dto = workCenterService.get(id);
        return ApiResponse.success(SuccessStatus.WORKCENTER_DETAIL_SUCCESS, dto);
    }

    @Operation(summary = "작업장 수정", description = "부분 수정(PATCH)")
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // 관리자만 수정 가능
    public ResponseEntity<ApiResponse<WorkCenterResponseDTO>> update(
            @PathVariable Long id,
            @RequestBody @Valid WorkCenterUpdateRequestDTO request
    ) {
        WorkCenterResponseDTO dto = workCenterService.update(id, request);
        return ApiResponse.success(SuccessStatus.WORKCENTER_UPDATE_SUCCESS, dto);
    }

    @Operation(summary = "작업장 삭제", description = "작업장 삭제")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // 관리자만 수정 가능
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        workCenterService.delete(id);
        return ApiResponse.success(SuccessStatus.WORKCENTER_DELETE_SUCCESS, null);
    }
}
