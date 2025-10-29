package com.sampoom.backend.api.part.service;

import com.sampoom.backend.api.part.dto.PartGroupCreateRequestDTO;
import com.sampoom.backend.api.part.dto.PartGroupUpdateRequestDTO;
import com.sampoom.backend.api.part.entity.PartCategory;
import com.sampoom.backend.api.part.entity.PartGroup;
import com.sampoom.backend.api.part.event.dto.PartGroupEvent;
import com.sampoom.backend.api.part.event.service.OutboxService;
import com.sampoom.backend.api.part.repository.PartCategoryRepository;
import com.sampoom.backend.api.part.repository.PartGroupRepository;
import com.sampoom.backend.common.exception.NotFoundException;
import com.sampoom.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PartGroupService {

    private final PartGroupRepository partGroupRepository;
    private final PartCategoryRepository categoryRepository;
    private final OutboxService outboxService;

    @Transactional
    public PartGroup createGroup(PartGroupCreateRequestDTO createRequestDTO) {

        PartCategory category = categoryRepository.findById(createRequestDTO.getCategoryId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.CATEGORY_NOT_FOUND));

        PartGroup newGroup = new PartGroup(
                createRequestDTO.getCode(),
                createRequestDTO.getName(),
                category
        );
        PartGroup savedGroup = partGroupRepository.save(newGroup);

        PartGroupEvent.Payload payload = PartGroupEvent.Payload.builder()
                .groupId(savedGroup.getId())
                .groupName(savedGroup.getName())
                .groupCode(savedGroup.getCode())
                .categoryId(category.getId())
                .build();

        outboxService.saveEvent(
                "PART_GROUP",
                savedGroup.getId(),
                "PartGroupCreated",
                savedGroup.getVersion(),
                payload
        );

        return savedGroup;
    }

    @Transactional
    public PartGroup updateGroup(Long groupId, PartGroupUpdateRequestDTO updateRequestDTO) {
        PartGroup partGroup = partGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.GROUP_NOT_FOUND));

        // PartGroup 엔티티에 update(code, name) 메서드가 있다고 가정
        partGroup.update(updateRequestDTO);

        PartGroupEvent.Payload payload = PartGroupEvent.Payload.builder()
                .groupId(partGroup.getId())
                .groupName(partGroup.getName())
                .groupCode(partGroup.getCode())
                .categoryId(partGroup.getCategory().getId())
                .build();

        outboxService.saveEvent(
                "PART_GROUP",
                partGroup.getId(),
                "PartGroupUpdated",
                partGroup.getVersion(),
                payload
        );

        return partGroup;
    }

    @Transactional
    public void deleteGroup(Long groupId) {
        PartGroup partGroup = partGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.GROUP_NOT_FOUND));

        partGroupRepository.delete(partGroup);

        PartGroupEvent.Payload payload = PartGroupEvent.Payload.builder()
                .groupId(partGroup.getId())
                .groupName(partGroup.getName())
                .groupCode(partGroup.getCode())
                .categoryId(partGroup.getCategory().getId())
                .build();

        outboxService.saveEvent(
                "PART_GROUP",
                partGroup.getId(),
                "PartGroupDeleted",
                partGroup.getVersion(),
                payload
        );
    }
}