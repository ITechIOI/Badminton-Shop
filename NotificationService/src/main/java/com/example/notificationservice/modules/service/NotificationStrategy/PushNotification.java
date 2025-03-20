package com.example.notificationservice.modules.service.NotificationStrategy;


import org.springframework.stereotype.Service;

@Service
public class PushNotification implements NotificationStrategy {
    @Override
    public void sendNotification(String to, String subject, String text) {
        System.out.println("Sending push notification");
    }
}