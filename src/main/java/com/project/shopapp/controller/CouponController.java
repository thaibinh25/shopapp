package com.project.shopapp.controller;


import com.project.shopapp.services.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @GetMapping("/apply")
    public ResponseEntity<?> applyCoupon(
            @RequestParam String code,
            @RequestParam double total
    ) {
        Map<String, Object> response = couponService.applyCoupon(code, total);
        return ResponseEntity.ok(response);
    }

}
