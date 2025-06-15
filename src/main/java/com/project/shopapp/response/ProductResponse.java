package com.project.shopapp.response;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.project.shopapp.dtos.ProductImageDTO;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse extends BaseResponse {

    private Long id;
    private String name;
    private Float price;
    private String thumbnail;
    private String description;
    @JsonProperty("product_images")
    private List<ProductImageDTO> productImages = new ArrayList<>();

    @JsonProperty("category_id")
    private Long categoryId;
    @JsonProperty("brand_id")
    private Long brandId;

    public static ProductResponse fromProduct(Product product) {
        ProductResponse productResponse = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .thumbnail(product.getThumbnail())
                .description(product.getDescription())
                .categoryId(product.getCategory().getId())
                .brandId(product.getBrand().getId())
                .productImages(
                        product.getProductImages()
                                .stream()
                                .map(ProductImageDTO::fromEntity)
                                .collect(Collectors.toList())
                )
                .build();
        productResponse.setCreatedAt(product.getCreatedAt());
        productResponse.setUpdatedAt(product.getUpdatedAt());
        return productResponse;
    }

}
