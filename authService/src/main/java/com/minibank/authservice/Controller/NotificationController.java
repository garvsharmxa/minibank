package com.minibank.authservice.Controller;

import com.minibank.authservice.Entity.Notification;
import com.minibank.authservice.Entity.UserPrincipal;
import com.minibank.authservice.Entity.Users;
import com.minibank.authservice.Repository.UserRepository;
import com.minibank.authservice.Services.NotificationService;
import com.minibank.authservice.dto.ApiResponse;
import com.minibank.authservice.dto.NotificationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationDto>>> getUserNotifications(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Users user = userRepository.findByUsername(userPrincipal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getUserNotifications(user, pageable);

        Page<NotificationDto> notificationDtos = notifications.map(this::convertToDto);

        ApiResponse<Page<NotificationDto>> response = ApiResponse.success(
                notificationDtos,
                "Notifications retrieved successfully",
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getUnreadNotifications(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Users user = userRepository.findByUsername(userPrincipal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications = notificationService.getUnreadNotifications(user);
        List<NotificationDto> notificationDtos = notifications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        ApiResponse<List<NotificationDto>> response = ApiResponse.success(
                notificationDtos,
                "Unread notifications retrieved successfully",
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Users user = userRepository.findByUsername(userPrincipal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        long count = notificationService.getUnreadCount(user);

        ApiResponse<Long> response = ApiResponse.success(
                count,
                "Unread count retrieved successfully",
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Object>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);

        ApiResponse<Object> response = ApiResponse.success(
                null,
                "Notification marked as read",
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Object>> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Users user = userRepository.findByUsername(userPrincipal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        notificationService.markAllAsRead(user);

        ApiResponse<Object> response = ApiResponse.success(
                null,
                "All notifications marked as read",
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    private NotificationDto convertToDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setIsRead(notification.getIsRead());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
}