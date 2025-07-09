package com.example.This_App_Backend.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notifications {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Integer notificationId;

    // Many Notifications belong to one User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Maps to user_id in notifications table
    private User user;

    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType;

    @Column(name = "notification_message", nullable = false, columnDefinition = "TEXT")
    private String notificationMessage;

    @Column(name = "is_read")
    private Boolean isRead;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
