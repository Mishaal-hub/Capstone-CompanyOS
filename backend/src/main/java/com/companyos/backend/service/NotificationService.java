package com.companyos.backend.service;

import com.companyos.backend.entity.Notification;
import java.util.List;

public interface NotificationService {
    void sendNotification(Long userId, String type, String title, String message, String entityType, Long entityId);
    List<Notification> getMyNotifications();
    void markAsRead(Long notificationId);
    void markAllAsRead();
    long getUnreadCount();
}
