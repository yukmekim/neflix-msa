package com.main.eureka.api.dto;

import lombok.*;

@Getter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> {
    private boolean success;
    private String code;
    private T data;
    private String message;

    public static <T> Response<T> payload(boolean success, String code, String message) {
        return Response.<T>builder()
                .success(success)
                .code(code)
                .message(message)
                .build();
    }

    public static <T> Response<T> payload(boolean success, String code, T data, String message) {
        return Response.<T>builder()
                .success(success)
                .code(code)
                .data(data)
                .message(message)
                .build();
    }
}
