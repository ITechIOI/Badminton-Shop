package com.example.notificationservice.modules.service.NotificationStrategy;

import com.example.notificationservice.modules.dto.CreateNotificationDto;
import org.springframework.stereotype.Service;

@Service
public interface NotificationStrategy {
    void sendNotification(String to, String subject, String text);
}
