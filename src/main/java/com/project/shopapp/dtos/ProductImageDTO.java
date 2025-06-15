package com.project.shopapp.dtos;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.shopapp.models.ProductImage;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductImageDTO {
    private long id;

    @JsonProperty("product_id")
    @Min(value = 1 , message = "Product's Id must be > 0")
    private long productId;

    @JsonProperty("image_url")
    @Size(min = 5,max = 200, message = "Image's name")
    private String imageUrl;

    public static ProductImageDTO fromEntity(ProductImage image) {
        return ProductImageDTO.builder()
                .id(image.getId())
                .productId(image.getProduct().getId())
                .imageUrl(image.getImageUrl())
                .build();
    }
}
