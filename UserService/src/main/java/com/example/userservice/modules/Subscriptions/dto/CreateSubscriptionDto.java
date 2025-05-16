package com.example.userservice.modules.Subscriptions.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateSubscriptionDto {
    private String endpoint;
    private String auth;
    private String p256dh;
    private Long userId;
}
