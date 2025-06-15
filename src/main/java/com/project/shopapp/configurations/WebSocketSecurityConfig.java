package com.project.shopapp.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class WebSocketSecurityConfig {
    @Bean
    @Order(1) // Ưu tiên áp dụng trước
    public SecurityFilterChain webSocketSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/ws/**") // áp dụng riêng cho /ws/**
                .authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable());

        return http.build();
    }
}
