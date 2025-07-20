package com.project.shopapp.controller;

import com.project.shopapp.dtos.PaymentRequest;
import com.project.shopapp.services.StripeService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    private final StripeService stripeService;

    @PostMapping("/create-payment-intent")
    public ResponseEntity<?> createPaymentIntent(@RequestBody PaymentRequest paymentRequest) {
        try {
            if (paymentRequest == null || paymentRequest.getAmount() == null) {
                return ResponseEntity.badRequest().body("Thiếu dữ liệu thanh toán.");
            }

            long amount = paymentRequest.getAmount();
            if (amount <= 0) {
                return ResponseEntity.badRequest().body("Số tiền thanh toán không hợp lệ.");
            }

            PaymentIntent intent = stripeService.createPaymentIntent(amount);
            return ResponseEntity.ok().body(Map.of("clientSecret", intent.getClientSecret()));

        } catch (Exception e) {
            log.error("🔥 Lỗi khi tạo PaymentIntent", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xử lý thanh toán: " + e.getMessage());
        }
    }
}
