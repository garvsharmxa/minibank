package com.minibank.authservice.listener;

import com.minibank.authservice.Services.NotificationService;
import com.minibank.authservice.Event.PasswordChangedEvent;
import com.minibank.authservice.Event.RoleChangedEvent;
import com.minibank.authservice.Event.UserCreatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {

    @Autowired
    private NotificationService notificationService;

    @Async
    @EventListener
    public void handleUserCreated(UserCreatedEvent event) {
        String title = "Welcome to MiniBank!";
        String message = String.format("Hello %s, your account has been successfully created. Welcome to MiniBank!",
                event.getUsername());
        notificationService.createNotificationByUsername(event.getUsername(), "USER_CREATED", title, message);
    }


    @Async
    @EventListener
    public void handlePasswordChanged(PasswordChangedEvent event) {
        String title = "Password Changed";
        String message = "Your password has been successfully changed. If you didn't make this change, please contact support immediately.";
        notificationService.createNotificationByUsername(event.getUsername(), "PASSWORD_CHANGED", title, message);
    }

    @Async
    @EventListener
    public void handleRoleChanged(RoleChangedEvent event) {
        String title = "Role " + event.getAction();
        String message = String.format("Your role has been %s: %s",
                event.getAction().toLowerCase(), event.getRoleName());
        notificationService.createNotificationByUsername(event.getUsername(), "ROLE_CHANGED", title, message);
    }
}
