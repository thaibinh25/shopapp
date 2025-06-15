package com.project.shopapp.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserResponse {
    private String fullName;
    @JsonProperty("phone_number")
    private String phoneNumber;

    private String address;

    @JsonProperty("date_of_birth")
    private Date dateOfBirth;
}
