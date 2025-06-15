package com.project.shopapp.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.shopapp.models.Notification;
import com.project.shopapp.models.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private String title;
    private String content;

    @JsonProperty("read")
    private boolean read;

    @JsonProperty("created_at")
    private Date createdAt;

    @JsonProperty("user_id")
    private Long userId;

    public NotificationResponse(Notification noti) {
        this.id = noti.getId();
        this.title = noti.getTitle();
        this.content = noti.getContent();
        this.read = noti.isRead();
        this.createdAt = noti.getCreatedAt();
        this.userId = noti.getUser() != null ? noti.getUser().getId() : null;
    }


    public NotificationResponse(Long id,String title, String content, boolean read, Date createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.read = read;
        this.createdAt = createdAt;
    }


}
