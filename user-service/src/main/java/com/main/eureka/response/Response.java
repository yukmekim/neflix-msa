package com.main.eureka.response;

import lombok.*;

@Getter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> {
    private boolean success;
    private String status;
    private T data;
    private String message;

    public static <T> Response<T> payload(boolean success, String status, String message) {
        return Response.<T>builder()
                .success(success)
                .status(status)
                .message(message)
                .build();
    }

    public static <T> Response<T> payload(boolean success, String status, T data, String message) {
        return Response.<T>builder()
                .success(success)
                .status(status)
                .data(data)
                .message(message)
                .build();
    }
}
