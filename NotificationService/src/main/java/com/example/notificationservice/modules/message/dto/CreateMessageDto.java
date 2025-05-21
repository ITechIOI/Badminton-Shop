package com.example.notificationservice.modules.message.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMessageDto {
    private String content;
    private String senderId;
    private String receiverId;
}
