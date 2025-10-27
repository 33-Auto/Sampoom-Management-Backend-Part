package com.sampoom.backend.api.part.dto;

import com.sampoom.backend.api.part.entity.PartStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PartUpdateRequestDTO {

    private String name;
    private PartStatus status;

    public void update(PartUpdateRequestDTO partUpdateRequestDTO) {

        if (partUpdateRequestDTO.getStatus() != null) {
            this.status = partUpdateRequestDTO.getStatus();
        }
    }
}
