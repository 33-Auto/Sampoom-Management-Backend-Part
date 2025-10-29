package com.sampoom.backend.api.workcenter.service;

import com.sampoom.backend.api.workcenter.entity.WorkCenter;
import com.sampoom.backend.api.workcenter.repository.WorkCenterRepository;
import com.sampoom.backend.api.workcenter.dto.WorkCenterCreateRequestDTO;
import com.sampoom.backend.api.workcenter.dto.WorkCenterResponseDTO;
import com.sampoom.backend.api.workcenter.dto.WorkCenterUpdateRequestDTO;
import com.sampoom.backend.api.workcenter.entity.WorkCenterStatus;
import com.sampoom.backend.api.workcenter.entity.WorkCenterType;
import com.sampoom.backend.common.exception.BadRequestException;
import com.sampoom.backend.common.exception.NotFoundException;
import com.sampoom.backend.common.response.ErrorStatus;
import com.sampoom.backend.common.response.PageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkCenterService {

    private final WorkCenterRepository workCenterRepository;

    /**
     * WorkCenter 코드 자동 생성 (WC-001 형태)
     */
    private String generateWorkCenterCode() {
        String lastCode = workCenterRepository.findLastWorkCenterCode();

        if (lastCode == null) {
            return "WC-001";
        }

        // WC-001에서 숫자 부분 추출
        String numberPart = lastCode.substring(3);
        int nextNumber = Integer.parseInt(numberPart) + 1;

        return String.format("WC-%03d", nextNumber);
    }

    @Transactional
    public WorkCenterResponseDTO create(WorkCenterCreateRequestDTO req) {
        String name = req.getName().trim();
        if (workCenterRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException(ErrorStatus.CONFLICT);
        }

        // 코드 자동 생성
        String generatedCode = generateWorkCenterCode();

        WorkCenter toSave = WorkCenter.builder()
                .code(generatedCode)
                .name(name)
                .type(req.getType())
                .status(req.getStatus())
                .dailyOperatingHours(req.getDailyOperatingHours())
                .efficiency(req.getEfficiency())
                .costPerHour(req.getCostPerHour())
                .build();

        WorkCenter saved = workCenterRepository.save(toSave);
        return WorkCenterResponseDTO.from(saved);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<WorkCenterResponseDTO> search(String q, WorkCenterType type, WorkCenterStatus status, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));

        String keyword = (q == null || q.isBlank()) ? null : q.trim();
        Page<WorkCenter> result = workCenterRepository.search(keyword, type, status, pageable);

        return PageResponseDto.<WorkCenterResponseDTO>builder()
                .content(result.map(WorkCenterResponseDTO::from).toList())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }
    @Transactional(readOnly = true)
    public WorkCenterResponseDTO get(Long id) {
        WorkCenter wc = workCenterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND));
        return WorkCenterResponseDTO.from(wc);
    }

    @Transactional
    public WorkCenterResponseDTO update(Long id, WorkCenterUpdateRequestDTO req) {
        WorkCenter wc = workCenterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND));

        if (req.getName() != null) {
            String name = req.getName().trim();
            if (name.isEmpty()) throw new BadRequestException(ErrorStatus.BAD_REQUEST);
            if (workCenterRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
                throw new BadRequestException(ErrorStatus.CONFLICT);
            }
            wc.changeName(name);
        }
        if (req.getType() != null) wc.changeType(req.getType());
        if (req.getStatus() != null) wc.changeStatus(req.getStatus());
        if (req.getDailyOperatingHours() != null) wc.changeDailyOperatingHours(req.getDailyOperatingHours());
        if (req.getEfficiency() != null) wc.changeEfficiency(req.getEfficiency());
        if (req.getCostPerHour() != null) wc.changeCostPerHour(req.getCostPerHour());

        return WorkCenterResponseDTO.from(wc);
    }

    @Transactional
    public void delete(Long id) {
        WorkCenter wc = workCenterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND));
        workCenterRepository.delete(wc);
    }
}
