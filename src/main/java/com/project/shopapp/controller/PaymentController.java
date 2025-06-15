package com.project.shopapp.controller;

import com.project.shopapp.dtos.PaymentRequest;
import com.project.shopapp.services.StripeService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
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
public class PaymentController {
    private final StripeService stripeService;

    @PostMapping("/create-payment-intent")
    public ResponseEntity<?> createPaymentIntent(@RequestBody Map<String, Object> request) {
        try {
            Number amountNumber = (Number) request.get("amount");
            long amount = amountNumber.longValue(); // ✅ ép kiểu an toàn
            //Long amount = Long.valueOf(request.get("amount").toString());
            PaymentIntent intent = stripeService.createPaymentIntent(amount);
            return ResponseEntity.ok(Map.of("clientSecret", intent.getClientSecret()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


}
