package com.example.notificationservice.modules.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateNotificationDto {
    private String title;
    private String content;
    private String type;
    private Long userId;
    private Long orderId;

    @Override
    public String toString() {
        return ", content=" + this.getContent() + ", type=" + this.getType() +
                ", userId=" + this.getUserId() + ", orderId=" + this.getOrderId() + ")";
    }
}
