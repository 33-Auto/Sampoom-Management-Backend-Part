package com.sampoom.backend.api.part.dto;

import com.sampoom.backend.api.part.entity.PartStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class PartUpdateRequestDTO {

    private String name;
    private PartStatus status;
    private String partUnit;
    private Integer baseQuantity;
    private Long groupId;
}
