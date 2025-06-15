package com.project.shopapp.services;

import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.models.Coupon;
import com.project.shopapp.repositories.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CouponService implements ICouponService{

    @Autowired
    private final CouponRepository couponRepository;



    @Override
    public Map<String, Object> applyCoupon(String code, double totalAmount) {
        Optional<Coupon> optionalCoupon = couponRepository.findByCodeAndActiveTrue(code);

        if (optionalCoupon.isEmpty()) {
            return Map.of(
                    "valid", false,
                    "message", "Mã giảm giá không tồn tại hoặc đã bị vô hiệu hóa"
            );
        }

        Coupon coupon = optionalCoupon.get();

        if (coupon.getExpiryDate().isBefore(LocalDate.now())) {
            return Map.of(
                    "valid", false,
                    "message", "Mã giảm giá đã hết hạn"
            );
        }

        double discountAmount = coupon.isPercentage()
                ? totalAmount * (coupon.getDiscountValue() / 100.0)
                : coupon.getDiscountValue();

        return Map.of(
                "valid", true,
                "discountAmount", discountAmount,
                "isPercentage", coupon.isPercentage(),
                "message", ""
        );
    }

}
