package com.project.shopapp.repositories;

import com.project.shopapp.models.Notification;
import com.project.shopapp.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,Long> {

    List<Notification> findByUserAndReadFalseOrderByCreatedAtDesc(User user);

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    long countByUserAndReadFalse(User user);

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByCreatedAtBefore(Date cutoff);

}
