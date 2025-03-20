package com.example.notificationservice.utils;

public record NotificationResponseKafka(
        String message
) {
    public String toString() {
        return "NotificationResponseKafka{" +
                "message='" + message + '\'' +
                '}';
    }
}
