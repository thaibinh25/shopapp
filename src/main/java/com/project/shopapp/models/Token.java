package com.project.shopapp.models;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "token", length = 255)
    private String token;

    @Column(name = "token_type", length = 50,nullable = true)
    private String tokenType;

    @Column(name = "expriration_date")
    private LocalDateTime exprirationDate;

    private boolean revoked;
    private boolean expired;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private  User user;
}
