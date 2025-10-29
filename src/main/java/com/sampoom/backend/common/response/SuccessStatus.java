package com.sampoom.backend.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum SuccessStatus {

    // 공통 성공 메시지
    OK(HttpStatus.OK, "요청이 성공적으로 처리되었습니다."),
    CREATED(HttpStatus.CREATED, "리소스가 성공적으로 생성되었습니다."),

    /**
     * 200
     */
    CATEGORY_LIST_SUCCESS(HttpStatus.OK, "카테고리 목록 조회 성공"),
    GROUP_LIST_SUCCESS(HttpStatus.OK, "그룹 목록 조회 성공"),
    PART_LIST_SUCCESS(HttpStatus.OK, "부품 목록 조회 성공"),
    PART_CREATE_SUCCESS(HttpStatus.OK, "부품 생성 성공"),
    PART_UPDATE_SUCCESS(HttpStatus.OK, "부품 수정 성공"),
    PART_DELETE_SUCCESS(HttpStatus.OK, "부품 삭제 성공"),
    PART_SEARCH_SUCCESS(HttpStatus.OK, "부품 검색 성공"),
    PART_DETAIL_SUCCESS(HttpStatus.OK, "부품 상세 조회 성공"),

    // 작업장
    WORKCENTER_CREATE_SUCCESS(HttpStatus.OK, "작업장 생성 성공"),
    WORKCENTER_LIST_SUCCESS(HttpStatus.OK, "작업장 목록 조회 성공"),
    WORKCENTER_DETAIL_SUCCESS(HttpStatus.OK, "작업장 상세 조회 성공"),
    WORKCENTER_UPDATE_SUCCESS(HttpStatus.OK, "작업장 수정 성공"),
    WORKCENTER_DELETE_SUCCESS(HttpStatus.OK, "작업장 삭제 성공"),

    MATERIAL_LIST_SUCCESS(HttpStatus.OK, "자재 목록 조회 성공"),
    ITEM_LIST_SUCCESS(HttpStatus.OK, "자재/부품 전체 조회 성공"),

    ;




    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}