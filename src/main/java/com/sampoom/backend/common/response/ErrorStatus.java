package com.sampoom.backend.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum ErrorStatus {

    // 400 BAD_REQUEST
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.", 30000),

    // 401 UNAUTHORIZED
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.", 10401),

    // 403 FORBIDDEN
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.",10406),

    // 404 NOT_FOUND
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다.", 30400),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다.", 30401),
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "그룹을 찾을 수 없습니다.", 30402),
    PART_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 부품을 찾을 수 없습니다.", 30403),
    MATERIAL_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 자재를 찾을 수 없습니다.", 30404),
    BOM_NOT_FOUND(HttpStatus.NOT_FOUND, "BOM을 찾을 수 없습니다.", 30405),

    // 409 CONFLICT
    CONFLICT(HttpStatus.CONFLICT, "충돌이 발생했습니다.",30900),
    PART_CODE_DUPLICATED(HttpStatus.CONFLICT, "이미 존재하는 부품 코드입니다.", 30901),

    DATA_CONFLICT(HttpStatus.CONFLICT, "다른 사용자에 의해 데이터가 수정되었습니다.", 30902),

    // 500 INTERNAL_SERVER_ERROR
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.", 10500);

    private final HttpStatus httpStatus;
    private final String message;
    private final int code;

    public int getStatusCode() {
        return this.httpStatus.value();
    }

}