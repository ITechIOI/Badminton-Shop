package com.example.notificationservice.modules.message.service;

import com.example.notificationservice.models.Messages;
import com.example.notificationservice.modules.message.dto.CreateMessageDto;
import com.example.notificationservice.modules.message.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;

    public Messages createMessage(CreateMessageDto message) {
        Messages newMessage = new Messages();
        newMessage.setContent(message.getContent());
        newMessage.setSenderId(message.getSenderId());
        newMessage.setReceiverId(message.getReceiverId());
        newMessage.setIsChecked("false");
        return messageRepository.save(newMessage);
    }
}
