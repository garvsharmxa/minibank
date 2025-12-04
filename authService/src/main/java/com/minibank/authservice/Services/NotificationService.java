package com.minibank.authservice.Services;

import com.minibank.authservice.Entity.Notification;
import com.minibank.authservice.Entity.Users;
import com.minibank.authservice.Repository.NotificationRepository;
import com.minibank.authservice.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${notification.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${notification.in-app.enabled:true}")
    private boolean inAppEnabled;

    @Value("${spring.mail.username:noreply@minibank.com}")
    private String fromEmail;

    @Transactional
    public void createNotification(Users user, String type, String title, String message) {
        if (inAppEnabled) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setType(type);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setIsRead(false);
            notificationRepository.save(notification);
        }

        if (emailEnabled && mailSender != null) {
            sendEmailNotification(user.getEmail(), title, message);
        }
    }

    @Transactional
    public void createNotificationByUsername(String username, String type, String title, String message) {
        userRepository.findByUsername(username).ifPresent(user -> 
            createNotification(user, type, title, message)
        );
    }

    private void sendEmailNotification(String to, String subject, String text) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(to);
            mailMessage.setSubject(subject);
            mailMessage.setText(text);
            mailSender.send(mailMessage);
        } catch (Exception e) {
            // Log error - in production, use proper logging framework like SLF4J
            // For now, using System.err as a minimal solution
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    public Page<Notification> getUserNotifications(Users user, Pageable pageable) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    public List<Notification> getUnreadNotifications(Users user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    public long getUnreadCount(Users user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
    }

    @Transactional
    public void markAllAsRead(Users user) {
        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        unreadNotifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }
}
