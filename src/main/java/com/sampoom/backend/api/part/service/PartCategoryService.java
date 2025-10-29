package com.sampoom.backend.api.part.service;

import com.sampoom.backend.api.part.dto.PartCategoryCreateRequestDTO;
import com.sampoom.backend.api.part.dto.PartCategoryUpdateRequestDTO;
import com.sampoom.backend.api.part.entity.PartCategory;
import com.sampoom.backend.api.part.event.dto.PartCategoryEvent;
import com.sampoom.backend.api.part.event.service.OutboxService;
import com.sampoom.backend.api.part.repository.PartCategoryRepository;
import com.sampoom.backend.common.exception.NotFoundException;
import com.sampoom.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PartCategoryService {

    private final PartCategoryRepository categoryRepository;
    private final OutboxService outboxService;

    @Transactional
    public PartCategory createCategory(PartCategoryCreateRequestDTO createRequestDTO) {

        PartCategory newCategory = new PartCategory(
                createRequestDTO.getCode(),
                createRequestDTO.getName()
        );
        PartCategory savedCategory = categoryRepository.save(newCategory);

        PartCategoryEvent.Payload payload = PartCategoryEvent.Payload.builder()
                .categoryId(savedCategory.getId())
                .categoryName(savedCategory.getName())
                .categoryCode(savedCategory.getCode())
                .build();

        outboxService.saveEvent(
                "PART_CATEGORY",
                savedCategory.getId(),
                "PartCategoryCreated",
                savedCategory.getVersion(),
                payload
        );

        return savedCategory;
    }

    @Transactional
    public PartCategory updateCategory(Long categoryId, PartCategoryUpdateRequestDTO updateRequestDTO) {

        // 카테고리 조회
        PartCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.CATEGORY_NOT_FOUND));

        category.update(updateRequestDTO);

        categoryRepository.flush();

        // Payload 생성
        PartCategoryEvent.Payload payload = PartCategoryEvent.Payload.builder()
                .categoryId(category.getId())
                .categoryName(category.getName())
                .categoryCode(category.getCode())
                .build();

        // OutboxService 호출
        outboxService.saveEvent(
                "PART_CATEGORY",
                category.getId(),
                "PartCategoryUpdated",
                category.getVersion(),
                payload
        );

        return category;
    }

    @Transactional
    public void deleteCategory(Long categoryId) {

        PartCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.CATEGORY_NOT_FOUND));

        categoryRepository.delete(category); // Hard Delete

        PartCategoryEvent.Payload payload = PartCategoryEvent.Payload.builder()
                .categoryId(category.getId())
                .categoryName(category.getName())
                .categoryCode(category.getCode())
                .build();

        outboxService.saveEvent(
                "PART_CATEGORY",
                category.getId(),
                "PartCategoryDeleted",
                category.getVersion(),
                payload
        );
    }
}