package com.minibank.authservice.listener;

import com.minibank.authservice.Repository.UserRepository;
import com.minibank.authservice.Services.AuditService;
import com.minibank.authservice.Services.NotificationService;
import com.minibank.authservice.event.LoginEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AuthEventListener {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private UserRepository userRepository;

    @Async
    @EventListener
    public void handleLoginEvent(LoginEvent event) {
        userRepository.findByUsername(event.getUsername()).ifPresent(user -> {
            if (event.isSuccess()) {
                // Log successful login
                auditService.logAction(user, "LOGIN_SUCCESS", "Successful login", event.getIpAddress());
                
                // Notify user of successful login
                String title = "Login Successful";
                String message = String.format("You have successfully logged in from IP: %s", event.getIpAddress());
                notificationService.createNotification(user, "LOGIN_SUCCESS", title, message);
            } else {
                // Log failed login
                auditService.logAction(user, "LOGIN_FAILED", event.getDetails(), event.getIpAddress());
                
                // Notify user of failed login attempt
                String title = "Failed Login Attempt";
                String message = String.format("A failed login attempt was detected for your account from IP: %s. If this wasn't you, please secure your account.", 
                        event.getIpAddress());
                notificationService.createNotification(user, "LOGIN_FAILED", title, message);
            }
        });
    }
}
