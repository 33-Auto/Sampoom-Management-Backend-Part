package com.sampoom.backend.api.part.dto;

import lombok.Getter;

@Getter
public class PartGroupCreateRequestDTO {

    private String code;
    private String name;
    private Long categoryId;
}
