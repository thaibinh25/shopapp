package com.project.shopapp.models;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_images")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

    public static  final int MAXIMUM_IMAGE_PER_PRODUCT = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "image_url")
    private String imageUrl;
}
