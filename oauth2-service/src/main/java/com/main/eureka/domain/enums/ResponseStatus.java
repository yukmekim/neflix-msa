package com.main.eureka.domain.enums;

import lombok.Getter;

public enum ResponseStatus {
    OK(200, "정상적인 응답"),
    BAD_REQUEST(400,"잘못된 요청"),
    UNAUTHORIZED(401, "인증 필요"),
    FORBIDDEN(403, "접근 금지"),
    NOT_FOUND(404, "찾을수 없는 리소스"),
    SERVER_ERROR(500, "서버 내부 오류"),
    SERVICE_UNAVAILABLE(503, "서비스 불가");

    private int code;
    private String description;

    ResponseStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }
}
