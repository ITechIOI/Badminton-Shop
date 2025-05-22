package com.example.reportservice.modules.message.service;

import com.example.reportservice.models.Messages;
import com.example.reportservice.modules.message.dto.CreateMessageDto;
import com.example.reportservice.modules.message.repository.MessageRepository;
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


