package com.example.reportservice.modules.message.repository;

import com.example.reportservice.models.Messages;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository  extends JpaRepository<Messages, Long> {
}
