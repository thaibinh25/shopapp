package com.project.shopapp.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.stripe.model.PaymentIntent;

import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService { @Value("${stripe.secret-key}")
private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey; // BẮT BUỘC phải gọi dòng này
    }

    public PaymentIntent createPaymentIntent(Long amount) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", amount);
        params.put("currency", "usd");
        return PaymentIntent.create(params);
    }

}
