package com.example.reportservice.modules.message.controller;

import com.example.reportservice.models.Messages;
import com.example.reportservice.modules.message.dto.CreateMessageDto;
import com.example.reportservice.modules.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    // client publish lên /app/chat/send
    @MessageMapping("/chat/send")
    public void processMessage(@Payload CreateMessageDto messageDTO) {
        // 1) Lưu message
        Messages saved = messageService.createMessage(messageDTO);
        System.out.println("New message: " + saved.getContent());

        // 2) Gửi ngược lại cho các client subscribe /topic/messages/{receiverId}
        messagingTemplate.convertAndSend(
                "/topic/messages/" + messageDTO.getReceiverId(),
                messageDTO
        );
    }
}
