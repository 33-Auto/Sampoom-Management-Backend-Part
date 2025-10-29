package com.sampoom.backend.api.process.service;

import com.sampoom.backend.api.part.entity.Part;
import com.sampoom.backend.api.part.repository.PartRepository;
import com.sampoom.backend.api.process.dto.*;
import com.sampoom.backend.api.process.entity.Process;
import com.sampoom.backend.api.process.entity.ProcessStep;
import com.sampoom.backend.api.process.entity.ProcessStatus;
import com.sampoom.backend.api.process.repository.ProcessRepository;
import com.sampoom.backend.api.workcenter.entity.WorkCenter;
import com.sampoom.backend.api.workcenter.repository.WorkCenterRepository;
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

import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class ProcessService {

    private final ProcessRepository processRepository;
    private final PartRepository partRepository;
    private final WorkCenterRepository workCenterRepository;

    /**
     * Process 코드 자동 생성 (PC-001 형태)
     */
    private String generateProcessCode() {
        String lastCode = processRepository.findLastProcessCode();

        if (lastCode == null) {
            return "PC-001";
        }

        // PC-001에서 숫자 부분 추출
        String numberPart = lastCode.substring(3);
        int nextNumber = Integer.parseInt(numberPart) + 1;

        return String.format("PC-%03d", nextNumber);
    }

    @Transactional
    public ProcessResponseDTO create(ProcessCreateRequestDTO req) {
        Part part = partRepository.findById(req.getPartId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.PART_NOT_FOUND));

        // 코드 자동 생성
        String generatedCode = generateProcessCode();

        Process process = Process.builder()
                .code(generatedCode)
                .part(part)
                .version(req.getVersion().trim())
                .status(req.getStatus())
                .build();

        // step 순서 기준 정렬 후 추가
        req.getSteps().stream()
                .sorted(Comparator.comparing(ProcessStepCreateRequestDTO::getStepOrder))
                .forEach(s -> {
                    WorkCenter wc = workCenterRepository.findById(s.getWorkCenterId())
                            .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND));

                    ProcessStep step = ProcessStep.builder()
                            .process(process)
                            .stepOrder(s.getStepOrder())
                            .stepName(s.getStepName().trim())
                            .workCenter(wc)
                            .setupMinutes(s.getSetupMinutes())
                            .processMinutes(s.getProcessMinutes())
                            .waitMinutes(s.getWaitMinutes())
                            .totalMinutes(0)
                            .build();
                    step.computeTotal();
                    process.addStep(step);
                });

        Process saved = processRepository.save(process);
        return new ProcessResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<ProcessResponseDTO> search(String q, ProcessStatus status, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));

        String keyword = (q == null || q.isBlank()) ? null : q.trim();
        Page<Process> result = processRepository.search(keyword, status, pageable);

        return PageResponseDto.<ProcessResponseDTO>builder()
                .content(result.map(ProcessResponseDTO::new).toList())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public ProcessResponseDTO get(Long id) {
        Process process = processRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND));
        return new ProcessResponseDTO(process);
    }

    @Transactional
    public ProcessResponseDTO update(Long id, ProcessUpdateRequestDTO req) {
        Process process = processRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND));

        // 부품 변경
        Part part = partRepository.findById(req.getPartId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.PART_NOT_FOUND));

        // 기존 스텝들 모두 삭제
        process.clearSteps();

        // 새로운 스텝들 추가
        req.getSteps().stream()
                .sorted(Comparator.comparing(ProcessStepCreateRequestDTO::getStepOrder))
                .forEach(s -> {
                    WorkCenter wc = workCenterRepository.findById(s.getWorkCenterId())
                            .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND));

                    ProcessStep step = ProcessStep.builder()
                            .process(process)
                            .stepOrder(s.getStepOrder())
                            .stepName(s.getStepName().trim())
                            .workCenter(wc)
                            .setupMinutes(s.getSetupMinutes())
                            .processMinutes(s.getProcessMinutes())
                            .waitMinutes(s.getWaitMinutes())
                            .totalMinutes(0)
                            .build();
                    step.computeTotal();
                    process.addStep(step);
                });

        // 기본 정보 수정
        process.changePart(part);
        process.changeVersion(req.getVersion().trim());
        process.changeStatus(req.getStatus());

        // 변경감지로 자동 반영
        return new ProcessResponseDTO(process);
    }

    @Transactional
    public void delete(Long id) {
        Process process = processRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND));
        processRepository.delete(process);
    }
}
