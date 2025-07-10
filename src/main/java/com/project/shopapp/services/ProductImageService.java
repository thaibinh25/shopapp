package com.project.shopapp.services;

import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.repositories.ProductImageRepository;
import com.project.shopapp.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;


import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductImageService implements IproductImageService{

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final S3Client s3Client;
    @Value("${aws.s3.bucket}")
    private String bucket;

    @Override
    public ProductImage getById(Long id) {
        return productImageRepository.findById(id).orElseThrow(null);
    }

    @Override
    public void deleteImage(Long id) throws DataNotFoundException {

        ProductImage image = productImageRepository.findById(id).orElseThrow(()->
                new DataNotFoundException("cannot find image!!!"));

        // Xoá file trên S3 (nếu muốn xoá thật)
        String key = image.getImageUrl().replace("https://"+ bucket +".s3.amazonaws.com/", "");
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (S3Exception e) {
            System.err.println("Failed to delete from S3: " + e.awsErrorDetails().errorMessage());
            throw e;
        }

        if (image != null) {
            Product product = image.getProduct();

            productImageRepository.deleteById(id);

            // Kiểm tra nếu ảnh bị xóa là thumbnail
            if (product.getThumbnail()!= null && product.getThumbnail().equals(image.getImageUrl())){
                List<ProductImage> productImages = productImageRepository.findByProductId(product.getId());
                if(productImages.isEmpty()){
                    product.setThumbnail(null);
                }else {
                    product.setThumbnail(productImages.get(0).getImageUrl());
                }
            }
            productRepository.save(product);
        }

    }



}
