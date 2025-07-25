package com.project.shopapp.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {

    @JsonProperty("user_id")
    @Min(value = 1,message = "User's ID must be > 0")
    private long userId;

    @JsonProperty("fullname")
    private String fullName;

    private String email;

    @JsonProperty("phone_number")
    @NotBlank(message = "Phone number is required")
    @Size(min = 10, message = "Phone number must be at least 10 characters")
    private String phoneNumber;

    @JsonProperty("address")
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


    private String note;

    @JsonProperty("total_money")
    @Min(value =  0, message = "Total Money must be >= 0")
    private Float totalMoney;

    @JsonProperty("shipping_method")
    private String shippingMethod;

    @JsonProperty("shipping_address")
    private String shippingAddress;

    @JsonProperty("shipping_date")
    private LocalDate shippingDate;

    @JsonProperty("payment_method")
    private String paymentMethod;

    @JsonProperty("coupon_code")
    private String couponCode;

    @JsonProperty("cart_items")
    private List<CartItemDTO> cartItems;
}
