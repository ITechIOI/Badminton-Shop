package com.example.notificationservice.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@SQLDelete(sql = "UPDATE #{#entityName} SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public abstract class AbstractModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;
}