package com.project.shopapp.repositories;

import com.project.shopapp.models.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon,Long> {
    Optional<Coupon> findByCodeAndActiveTrue(String code);
    Optional<Coupon> findByCode(String code);
}
