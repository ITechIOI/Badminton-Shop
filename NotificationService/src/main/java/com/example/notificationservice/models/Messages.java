package com.example.notificationservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "messages")
@Getter
@Setter
public class Messages extends AbstractModel {
    @Column(nullable = true)
    private String content;

    @Column(nullable = true)
    private String senderId;

    @Column(nullable = true)
    private String receiverId;

    @Column(nullable = true)
    private String isChecked;
}
