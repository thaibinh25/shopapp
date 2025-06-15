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
public class UpdateBrandResponse {
    @JsonProperty("message")
    private String message;
}
