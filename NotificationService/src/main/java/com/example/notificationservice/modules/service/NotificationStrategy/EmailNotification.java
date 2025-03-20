package com.example.notificationservice.modules.service.NotificationStrategy;

import com.example.notificationservice.modules.dto.CreateNotificationDto;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;

@Service
public class EmailNotification implements NotificationStrategy {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailNotification(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendNotification(String to, String subject, String text) {
        System.out.println("Sending gmail notification");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
