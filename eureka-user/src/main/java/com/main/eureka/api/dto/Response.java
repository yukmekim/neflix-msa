package com.main.eureka.api.dto;

import lombok.*;

@Getter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> {
    private boolean success;
    private T data;
    private String message;

    public static <T> Response<T> payload(boolean success, String message) {
        return Response.<T>builder()
                .success(success)
                .message(message)
                .build();
    }

    public static <T> Response<T> payload(boolean success, T data, String message) {
        return Response.<T>builder()
                .success(success)
                .data(data)
                .message(message)
                .build();
    }
}
