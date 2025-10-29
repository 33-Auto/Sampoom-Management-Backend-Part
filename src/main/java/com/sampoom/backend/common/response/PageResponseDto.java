package com.sampoom.backend.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDto<T> {
    private List<T> content;         // 실제 데이터
    private long totalElements;      // 총 요소 수
    private int totalPages;          // 총 페이지 수
}