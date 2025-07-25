package com.project.shopapp.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDTO {

    @JsonProperty("fullname")
    private String fullName;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("date_of_birth")
    private Date dateOfBirth;


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


    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("email")
    private String email;


}
