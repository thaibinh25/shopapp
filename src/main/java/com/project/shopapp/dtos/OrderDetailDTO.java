package com.project.shopapp.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import lombok.*;

@Data
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailDTO {
    @JsonProperty("order_id")
    @Min(value = 1,message = "Order's ID must be > 0 ")
    private long orderId;

    @JsonProperty("product_id")
    @Min(value = 1,message = "Order's ID must be > 0 ")
    private long productId;

    @Min(value = 0,message = "Order's ID must be >= 0 ")
    private Float price;

    @JsonProperty("number_of_products")
    @Min(value = 1,message = "Order's ID must be > 0 ")
    private  Integer numberOfProducts;

    @JsonProperty("total_money")
    @Min(value = 0,message = "Order's ID must be >= 0 ")
    private Float totalMoney;

    private String color;


}
