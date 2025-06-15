package com.project.shopapp.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "social_accounts")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "provider", length = 20, nullable = false)
    private String provider;

    @Column(name = "provider_id", length = 50, nullable = false)
    private String providerId;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "email", length = 150)
    private String email;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private  User user;

}
