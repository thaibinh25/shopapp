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

    @JsonProperty("zip_code")
    private String zipCode;

    @JsonProperty("prefecture")
    private String prefecture;

    @JsonProperty("city")
    private String city;

    @JsonProperty("address_line1")
    private String addressLine1;

    @JsonProperty("address_line2")
    private String addressLine2;

    @JsonProperty("date_of_birth")
    private Date dateOfBirth;
}
