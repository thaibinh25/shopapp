package com.project.shopapp.dtos;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {

    @JsonProperty("current_password")
    private String currentPassword;

    @JsonProperty("new_password")
    private String newPassword;
}
