package com.example.notificationservice.modules.message.controller;

import com.example.notificationservice.models.Messages;
import com.example.notificationservice.modules.message.dto.CreateMessageDto;
import com.example.notificationservice.modules.message.repository.MessageRepository;
import com.example.notificationservice.modules.message.service.MessageService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications/messages")
@AllArgsConstructor
public class MessageController {
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    @MessageMapping("/chat/send") // Gửi từ client tới: /app/chat/send
    public void processMessage(CreateMessageDto messageDTO) {
        Messages saved = messageService.createMessage(messageDTO);

        System.out.println("New message: " + saved.getContent());

        // Gửi lại cho người nhận theo channel riêng
        messagingTemplate.convertAndSend(
                "/topic/messages/" + messageDTO.getReceiverId(), messageDTO);
    }
}
