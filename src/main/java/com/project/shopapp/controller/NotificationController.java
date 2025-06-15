package com.project.shopapp.controller;

import com.project.shopapp.WebSocketServer;
import com.project.shopapp.dtos.AllUserNotificationDTO;
import com.project.shopapp.dtos.NotificationDTO;
import com.project.shopapp.models.Notification;
import com.project.shopapp.models.User;
import com.project.shopapp.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.project.shopapp.response.NotificationResponse;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
       private final NotificationService  notificationService;


    // Gửi thông báo đến toàn bộ user thường (ROLE_USER)
    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> broadcast(@RequestBody AllUserNotificationDTO request) {
        notificationService.broadcastToUsers(request);
        return ResponseEntity.ok(Map.of("message", "Đã gửi thông báo đến tất cả người dùng."));
    }


    @GetMapping
    public ResponseEntity<?> getAllUserNotifications(@RequestParam("userId") Long userId) {
        return ResponseEntity.ok(notificationService.getAllNotifications(userId));
    }

    // chỉ gửi thông báo đến 1 khách hàng nào đó thôi .
    // khi mà order, update thì sẽ dùng cái này để thông báo khách được thực thi hành động
    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody NotificationDTO request) {
        notificationService.createAndSendNotification(request);
        return ResponseEntity.ok(Map.of("message", "Đã gửi thông báo đến người dùng  "));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(Map.of("message", "Đã đánh dấu là đã đọc"));
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> getNotificationById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        try {
            NotificationResponse response = notificationService.getNotificationById(id, currentUser);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notification not found");
        }
    }


}
