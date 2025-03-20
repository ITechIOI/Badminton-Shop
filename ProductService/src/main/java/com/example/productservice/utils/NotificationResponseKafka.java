package com.example.productservice.utils;

public record NotificationResponseKafka(
        String message
) {
    public String toString() {
        return "NotificationResponseKafka{" +
                "message='" + message + '\'' +
                '}';
    }
}
