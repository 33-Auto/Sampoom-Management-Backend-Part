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
    CATEGORY_IN_USE(HttpStatus.BAD_REQUEST, "해당 카테고리를 사용하는 그룹이 존재하여 삭제할 수 없습니다.", 30001),
    DUPLICATE_BOM(HttpStatus.BAD_REQUEST, "이미 해당 부품의 BOM이 존재합니다. 수정으로 진행해주세요.", 30002),


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
    WORK_CENTER_NOT_FOUND(HttpStatus.NOT_FOUND, "작업장을 찾을 수 없습니다.", 30406),

    // 409 CONFLICT
    CONFLICT(HttpStatus.CONFLICT, "충돌이 발생했습니다.",30900),
    PART_CODE_DUPLICATED(HttpStatus.CONFLICT, "이미 존재하는 부품 코드입니다.", 30901),

    DATA_CONFLICT(HttpStatus.CONFLICT, "다른 사용자에 의해 데이터가 수정되었습니다.", 30902),

    // 400 BAD_REQUEST
    SHORT_PUBLIC_KEY(HttpStatus.BAD_REQUEST, "서명용 공개키의 길이가 짧습니다. 적어도 2048비트 이상으로 설정하세요.", 12401),
    NULL_BLANK_TOKEN(HttpStatus.BAD_REQUEST, "토큰 값은 Null 또는 공백이면 안됩니다.", 12400),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "유효하지 않은 입력 값입니다.", 11402),
    INVALID_WORKSPACE_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 조직(workspace) 타입입니다.", 11401),
    BLANK_TOKEN_ROLE(HttpStatus.BAD_REQUEST,"토큰 내 권한 정보가 공백입니다.",12404),
    NULL_TOKEN_ROLE(HttpStatus.BAD_REQUEST,"토큰 내 권한 정보가 NULL입니다.",12405),
    INVALID_REQUEST_ORGID(HttpStatus.BAD_REQUEST,"workspace 없이 organizationID로만 요청할 수 없습니다.",11403),
    INVALID_EMPSTATUS_TYPE(HttpStatus.BAD_REQUEST,"유효하지 않은 직원 상태(EmployeeStatus) 타입입니다.",11404),
    INVALID_PUBLIC_KEY(HttpStatus.BAD_REQUEST,"서명용 공개키가 유효하지 않거나 불러오는데 실패했습니다.",12406),

    // 401 UNAUTHORIZED
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.", 12410),
    NOT_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED,"토큰의 타입이 액세스 토큰이 아닙니다.",12413),
    NOT_SERVICE_TOKEN(HttpStatus.UNAUTHORIZED,"토큰의 타입이 서비스 토큰(내부 통신용 토큰)이 아닙니다.",12414),
    INVALID_TOKEN_ROLE(HttpStatus.UNAUTHORIZED,"유효하지 않은 토큰 내 권한 정보입니다. (토큰 권한 불일치)",12415),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다.", 12411),

    // 403 FORBIDDEN
    ACCESS_DENIED(HttpStatus.FORBIDDEN,"접근 권한이 없어 접근이 거부되었습니다.",11430),

    // 404 NOT_FOUND
    NOT_FOUND_USER_BY_ID(HttpStatus.NOT_FOUND, "유저 고유 번호(userId)로 해당 유저를 찾을 수 없습니다.", 11440),
    NOT_FOUND_FACTORY_NAME(HttpStatus.NOT_FOUND, "지점명으로 공장 이름을 찾을 수 없습니다.", 10440),
    NOT_FOUND_WAREHOUSE_NAME(HttpStatus.NOT_FOUND, "지점명으로 창고 이름을 찾을 수 없습니다.", 10441),
    NOT_FOUND_AGENCY_NAME(HttpStatus.NOT_FOUND, "지점명으로 대리점 이름을 찾을 수 없습니다.", 10442),
    NOT_FOUND_FACTORY_EMPLOYEE(HttpStatus.NOT_FOUND,"전체 공장에서 해당 직원을 찾을 수 없습니다.",11442),
    NOT_FOUND_WAREHOUSE_EMPLOYEE(HttpStatus.NOT_FOUND,"전체 창고에서 해당 직원을 찾을 수 없습니다.",11443),
    NOT_FOUND_AGENCY_EMPLOYEE(HttpStatus.NOT_FOUND,"전체 대리점에서 해당 직원을 찾을 수 없습니다.",11444),

    // 409 CONFLICT
    DUPLICATED_USER_ID(HttpStatus.CONFLICT, "이미 존재하는 유저의 ID입니다.", 11491),

    // 500 INTERNAL_SERVER_ERROR
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.", 10500),
    INVALID_EVENT_FORMAT(HttpStatus.INTERNAL_SERVER_ERROR, "이벤트 형식이 유효하지 않습니다.", 10501),
    FAILED_CONNECTION_KAFKA(HttpStatus.INTERNAL_SERVER_ERROR,"Kafka 브로커 연결 또는 통신에 실패했습니다.",10503),
    EVENT_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Kafka 이벤트 처리 중 예외가 발생했습니다.",10504),
    OUTBOX_SERIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"Outbox 직렬화에 실패했습니다.",10505),


    TOO_SHORT_SECRET_KEY(HttpStatus.BAD_REQUEST, "서명용 비밀키의 길이가 짧습니다. 적어도 32바이트 이상으로 설정하세요.", 12402),
    NULL_TOKEN(HttpStatus.BAD_REQUEST, "토큰 값은 Null이면 안됩니다.", 12401),
    BLANK_TOKEN(HttpStatus.BAD_REQUEST, "토큰 값은 공백이면 안됩니다.", 12400),
    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final int code;

    public int getStatusCode() {
        return this.httpStatus.value();
    }

}