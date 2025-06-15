package com.project.shopapp.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name= "coupons")
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    private double discountValue; // Ví dụ: 10.0 (nghĩa là 10% hoặc 10000 tùy loại)
    private boolean isPercentage; // true = %, false = số tiền cố định
    private boolean active;

    private LocalDate expiryDate;

}
