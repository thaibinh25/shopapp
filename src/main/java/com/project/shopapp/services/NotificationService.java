package com.project.shopapp.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.shopapp.dtos.AllUserNotificationDTO;
import com.project.shopapp.dtos.NotificationDTO;
import com.project.shopapp.models.Notification;
import com.project.shopapp.models.Role;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.NotificationRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.response.NotificationResponse;
import lombok.RequiredArgsConstructor;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import com.project.shopapp.WebSocketServer;

@Service
@RequiredArgsConstructor
public class NotificationService {
   // private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public Notification createAndSendNotification(NotificationDTO request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        Notification notification = Notification.builder()
                .user(userRepository.findById(request.getUserId()).orElseThrow())
                .title(request.getTitle())
                .content(request.getContent())
                .read(false)
                .createdAt(new Date())
                .build();
        notificationRepository.save(notification);

        /*try {
            String json = objectMapper.writeValueAsString(notification);
            WebSocketServer.sendNotificationToUser(request.getUserId(), json); // ✅ Gửi JSON
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        try {
            // Tạo DTO hoặc response để tránh gửi toàn bộ entity (tránh vòng lặp hoặc trường không cần thiết)
            NotificationResponse response = new NotificationResponse(notification); // hoặc tự build DTO
            String json = objectMapper.writeValueAsString(response);

            WebSocketServer.sendNotificationToUser(user.getId(), json); // ✅ Gửi JSON chuẩn
        } catch (Exception e) {
            System.err.println("❌ Failed to send WebSocket notification: " + e.getMessage());
            e.printStackTrace();
        }


        return notification;
    }

    public List<NotificationResponse> getAllNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(n -> new NotificationResponse(
                        n.getId(),n.getTitle(), n.getContent(), n.isRead(), n.getCreatedAt()))
                .collect(Collectors.toList());
    }

    // ✅ Lấy danh sách notification của user theo số điện thoại
    public List<Notification> getNotificationsByPhone(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User not found with phone: " + phoneNumber));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    // ✅ Đánh dấu 1 thông báo là đã đọc
    public void markAsRead(Long notificationId) {
        Notification noti = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));
        noti.setRead(true);
        notificationRepository.save(noti);
    }


    public NotificationResponse getNotificationById(Long id, User currentUser) {
        Notification noti = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (noti.getUser().getId() != currentUser.getId()) {
            throw new SecurityException("Unauthorized access to this notification");
        }

        return new NotificationResponse(noti);
    }


    public void broadcastToUsers(AllUserNotificationDTO request) {
        //Dùng query JPA viết ở repositori cũng được  hoặc tìm trực tiếp như dứoi
        List<User> users = userRepository.findAllUsersWithUserRole();

        /*List<User> users = userRepository.findAll()
                .stream()
                .filter(u -> {
                    Role role = u.getRole();
                    return role != null && "user".equalsIgnoreCase(role.getName());
                })
                .collect(Collectors.toList());*/

        for (User user : users) {
            Notification notification = Notification.builder()
                    .user(user)
                    .title(request.getTitle())
                    .content(request.getContent())
                    .read(false)
                    .createdAt(new Date())
                    .build();

            notificationRepository.save(notification);

            try {
                String json = objectMapper.writeValueAsString(new NotificationResponse(notification));
                WebSocketServer.sendNotificationToUser(user.getId(), json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Scheduled(cron = "0 0 3 * * ?") // chạy mỗi ngày lúc 3h sáng
    public void deleteOldNotifications() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        Date cutoff = Date.from(sixMonthsAgo.atZone(ZoneId.systemDefault()).toInstant());
        notificationRepository.deleteByCreatedAtBefore(cutoff);
    }


}
