package com.example.notificationservice.modules.dto;

import lombok.Data;

@Data
public class PushNotificationDto {
    private String endpoint;
    private String p256dh;
    private String auth;
}
