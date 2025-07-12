package com.project.shopapp.configurations;

import com.project.shopapp.filters.JwtTokenFilter;
import com.project.shopapp.models.Role;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpMethod.*;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity(debug = true)
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtTokenFilter jwtTokenFilter;

    @Value("${api.prefix}")
    private String apiPrefix;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);

        // ✅ CHO PHÉP ORIGIN NGROK
        config.setAllowedOrigins(List.of(
                "https://thaibinhshop.duckdns.org",
                "https://ffc5791377bf.ngrok-free.app"
        ));
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/api/v1/products/images/**");
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/v1/products/images/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET");
            }
        };
    }



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
         http
                 .cors(Customizer.withDefaults())
                 .csrf(AbstractHttpConfigurer::disable)
                 .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                 .authorizeHttpRequests(requests ->{
                     requests
                             .requestMatchers("/error").permitAll()
                             .requestMatchers(
                                     String.format("%s/users/register", apiPrefix),
                                     String.format("%s/users/login", apiPrefix)
                             ).permitAll()
                             .requestMatchers(POST, String.format("%s/users/details", apiPrefix)).permitAll()

                             .requestMatchers(GET,
                                     String.format("%s/roles**", apiPrefix)).permitAll()

                             .requestMatchers(GET,
                                     String.format("%s/categories**", apiPrefix)).permitAll()
                             .requestMatchers(GET,
                                     String.format("%s/categories", apiPrefix)).permitAll()
                             .requestMatchers(POST,
                                     String.format("%s/categories/**", apiPrefix)).hasRole(Role.ADMIN)
                             .requestMatchers(PUT,
                                     String.format("%s/categories/**", apiPrefix)).hasRole(Role.ADMIN)
                             .requestMatchers(DELETE,
                                     String.format("%s/categories/**", apiPrefix)).hasRole(Role.ADMIN)

                             .requestMatchers(GET,
                                     String.format("%s/brands**", apiPrefix)).permitAll()
                             .requestMatchers(GET,
                                     String.format("%s/brands/**", apiPrefix)).permitAll()
                             .requestMatchers(GET,
                                     String.format("%s/brands", apiPrefix)).permitAll()
                             .requestMatchers(POST,
                                     String.format("%s/brands/**", apiPrefix)).hasRole(Role.ADMIN)
                             .requestMatchers(PUT,
                                     String.format("%s/brands/**", apiPrefix)).hasRole(Role.ADMIN)
                             .requestMatchers(DELETE,
                                     String.format("%s/brands/**", apiPrefix)).hasRole(Role.ADMIN)

                             .requestMatchers(GET,
                                     String.format("%s/orders/users/**", apiPrefix)).hasRole(Role.USER)
                             .requestMatchers(GET,
                                     String.format("%s/orders/**", apiPrefix)).permitAll()
                             .requestMatchers(POST,
                                     String.format("%s/orders/**", apiPrefix)).hasRole(Role.USER)
                             .requestMatchers(PUT,
                                     String.format("%s/orders/**", apiPrefix)).hasRole(Role.ADMIN)
                             .requestMatchers(DELETE,
                                     String.format("%s/orders/**", apiPrefix)).hasRole(Role.ADMIN)

                             .requestMatchers(GET,
                                     String.format("%s/products", apiPrefix)).permitAll()
                             .requestMatchers(GET,
                                     String.format("%s/products/**", apiPrefix)).permitAll()
                             .requestMatchers(GET,
                                     String.format("%s/products/images/**", apiPrefix)).permitAll()
                             .requestMatchers(POST,
                                     String.format("%s/products/**", apiPrefix)).hasRole(Role.ADMIN)
                             .requestMatchers(PUT,
                                     String.format("%s/products/**", apiPrefix)).hasRole(Role.ADMIN)
                             .requestMatchers(DELETE,
                                     String.format("%s/products/**", apiPrefix)).hasRole(Role.ADMIN)


                             .requestMatchers(PUT,
                                     String.format("%s/productImages/**", apiPrefix)).hasRole(Role.ADMIN)
                             .requestMatchers(DELETE,
                                     String.format("%s/productImages/**", apiPrefix)).hasRole(Role.ADMIN)

                             .requestMatchers(GET,
                                     String.format("%s/order_details/**", apiPrefix)).permitAll()
                             .requestMatchers(POST,
                                     String.format("%s/order_details/**", apiPrefix)).hasRole(Role.ADMIN)
                             .requestMatchers(PUT,
                                     String.format("%s/order_details/**", apiPrefix)).hasRole(Role.ADMIN)
                             .requestMatchers(DELETE,
                                     String.format("%s/order_details/**", apiPrefix)).hasRole(Role.ADMIN)


                             .requestMatchers("/ws/**").permitAll() // Cho phép WebSocket
                             .requestMatchers("/wss/**").permitAll() // Cho phép WebSocket

                             .requestMatchers(HttpMethod.POST, String.format("%s/payments/create-payment-intent", apiPrefix)).hasRole(Role.USER)

                             .requestMatchers(String.format("%s/notifications/**", apiPrefix)).authenticated()

                             .requestMatchers(HttpMethod.POST, String.format("%s/notifications/send/**", apiPrefix)).hasRole(Role.ADMIN)

                             .requestMatchers(HttpMethod.POST, String.format("%s/notifications/broadcast**", apiPrefix)).hasRole(Role.ADMIN)

                             .requestMatchers(HttpMethod.POST, String.format("%s/auth/google", apiPrefix)).permitAll()

                             .requestMatchers("/api/admin/rename-files").permitAll()
                             .anyRequest().authenticated();
                 })
                 .csrf(AbstractHttpConfigurer::disable);

        // 👇 Chỉ thêm JwtTokenFilter vào những request không phải websocket
        http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        /*http.cors(new Customizer<CorsConfigurer<HttpSecurity>>() {




            @Override
            public void customize(CorsConfigurer<HttpSecurity> httpSecurityCorsConfigurer) {

                CorsConfiguration configuration = new CorsConfiguration();

                //configuration.setAllowCredentials(true);//websocket cần khi gửi kèm token


                configuration.setAllowedOrigins(List.of("*"));
                configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
                //configuration.setAllowedHeaders(Arrays.asList("authorization","content-type","x-auth-token"));
                configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Auth-Token"));
                configuration.setExposedHeaders(List.of("x-auth-token"));
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**",configuration);
                httpSecurityCorsConfigurer.configurationSource(source);
            }
        });*/

         return http.build();
    }
}
