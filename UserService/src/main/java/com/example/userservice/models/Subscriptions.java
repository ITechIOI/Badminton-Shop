package com.example.userservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscriptions extends AbstractModel{
    private String endpoint;
    private String auth;
    private String p256dh;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private Users user;
}
