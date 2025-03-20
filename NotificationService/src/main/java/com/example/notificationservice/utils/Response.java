package com.example.notificationservice.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Response<T> {
    private int status;
    private String message;
    private T data;

    public Response(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> Response<T> success(T data) {
        return new Response<>(200, "Success", data);
    }

    public static <T> Response<T> error(int status, String message) {
        return new Response<>(status, message, null);
    }

}
