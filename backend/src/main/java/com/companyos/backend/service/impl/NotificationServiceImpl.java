package com.companyos.backend.service.impl;

import com.companyos.backend.entity.Notification;
import com.companyos.backend.entity.User;
import com.companyos.backend.repository.NotificationRepository;
import com.companyos.backend.repository.UserRepository;
import com.companyos.backend.security.TenantContext;
import com.companyos.backend.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void sendNotification(Long userId, String type, String title, String message, String entityType, Long entityId) {
        Long orgId = TenantContext.getTenantId();
        if (orgId == null || userId == null) return;

        Notification notification = new Notification(orgId, userId, type, title, message);
        notification.setReferenceEntityType(entityType);
        notification.setReferenceEntityId(entityId);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getMyNotifications() {
        Long userId = getCurrentUserId();
        if (userId == null) return Collections.emptyList();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            Long currentUserId = getCurrentUserId();
            if (currentUserId != null && currentUserId.equals(n.getUserId())) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        Long userId = getCurrentUserId();
        if (userId == null) return;
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        for (Notification n : unread) {
            n.setRead(true);
        }
        notificationRepository.saveAll(unread);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        Long userId = getCurrentUserId();
        if (userId == null) return 0;
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            return userRepository.findByUsername(auth.getName()).map(User::getUserId).orElse(null);
        }
        return null;
    }
}
