package com.example.notificationservice.modules.message.repository;

import com.example.notificationservice.models.Messages;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository  extends JpaRepository<Messages, Long> {
}
