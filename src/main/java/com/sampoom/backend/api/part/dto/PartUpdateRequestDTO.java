package com.sampoom.backend.api.part.dto;

import com.sampoom.backend.api.part.entity.PartStatus;
import com.sampoom.backend.api.part.entity.ProcurementType;
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
    private Integer leadTime;
    private Long groupId;

    private ProcurementType procurementType;
    private BigDecimal standardCost;
}
