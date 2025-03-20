package com.example.notificationservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notifications extends AbstractModel {
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    @Pattern(regexp = "push|gmail|otp", message = "The notification type is invalid")
    private String type;

    @Column(nullable = true)
    private Long userId;

    @Column(nullable = true)
    private Long orderId;
}
