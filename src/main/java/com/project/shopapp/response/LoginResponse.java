package com.project.shopapp.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.MappedSuperclass;
import lombok.*;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@Builder
public class LoginResponse {
    @JsonProperty("message")
    private String message;

    @JsonProperty("token")
    private String token;

    @JsonProperty("role")
    private String role;
}
