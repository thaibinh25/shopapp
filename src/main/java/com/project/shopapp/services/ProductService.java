package com.project.shopapp.services;

import com.project.shopapp.dtos.ProductDTO;
import com.project.shopapp.dtos.ProductImageDTO;
import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.exception.InvalidParamException;
import com.project.shopapp.models.Brand;
import com.project.shopapp.models.Category;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.repositories.BrandRepository;
import com.project.shopapp.repositories.CategoryRepository;
import com.project.shopapp.repositories.ProductImageRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService implements IProductService{
    private final S3Client s3Client;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final BrandRepository brandRepository;

    @Value("${aws.s3.bucket}")
    private String bucket;


    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product createProduct(ProductDTO productDTO) throws DataNotFoundException {
        Category existingCategory = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(()-> new DataNotFoundException(
                        "cannot find category with id "+ productDTO.getCategoryId()));
        Brand exsistingBrand = brandRepository.findById(productDTO.getBrandId())
                .orElseThrow(()-> new DataNotFoundException(
                        "cannot find brand with id" + productDTO.getBrandId()));

        Product newProduct = Product.builder()
                .name(productDTO.getName())
                .price(productDTO.getPrice())
                .thumbnail(null)
                .description(productDTO.getDescription())
                .category(existingCategory)
                .brand(exsistingBrand)
                .build();
        return productRepository.save(newProduct);
    }



    @Override
    public Product getProductById(long productId) throws Exception {
        Optional<Product> optionalProduct = productRepository.getDetailProduct(productId);
        if(optionalProduct.isPresent()) {
            return optionalProduct.get();
        }
        throw new DataNotFoundException("Cannot find product with id =" + productId);
    }

    @Override
    public List<ProductResponse> findProductsByIds(List<Long> productIds) {
        List<Product> products = productRepository.findProductsByIds(productIds);
        return products.stream()
                .map(ProductResponse::fromProduct)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductResponse> getAllProducts(
            String keyword,
            Long categoryId,
            Long brandId,
            Double minPrice,
            Double maxPrice,
            PageRequest pageRequest) {
        //Lấy danh sách sản phẩm theo trang (page) và giới hạn (limit)
        log.info("keyword={}, categoryId={}, brandId={}, minPrice={}, maxPrice={}",
                keyword, categoryId, brandId, minPrice, maxPrice);
        Page<Product> productPage = productRepository.searchProducts(keyword,categoryId,brandId, minPrice, maxPrice,pageRequest);
        return productPage.map(ProductResponse::fromProduct);

    }

    @Override
    @Transactional
    public Product updateProduct(long id, ProductDTO productDTO) throws Exception {
        Category existingCategory = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(()-> new DataNotFoundException(
                        "cannot find category with id "+ productDTO.getCategoryId()));
        Product existingProduct = getProductById(id);
        if (existingProduct!= null ){
            existingProduct.setName(productDTO.getName());
            existingProduct.setCategory(existingCategory);
            existingProduct.setPrice(productDTO.getPrice());
            //existingProduct.setThumbnail(productDTO.getThumbnail());
            existingProduct.setDescription(productDTO.getDescription());

            // --- Thumbnail Handling ---
            String newThumbnail = productDTO.getThumbnail();
            boolean thumbnailStillExists = existingProduct.getProductImages().stream()
                    .anyMatch(img -> img.getImageUrl().equals(newThumbnail));

            if (thumbnailStillExists) {
                // Giữ lại thumbnail từ DTO nếu ảnh đó vẫn còn
                existingProduct.setThumbnail(newThumbnail);
            } else if (!existingProduct.getProductImages().isEmpty()) {
                // Nếu ảnh đó bị xoá, gán lại thumbnail là ảnh đầu tiên còn lại
                existingProduct.setThumbnail(existingProduct.getProductImages().get(0).getImageUrl());
            } else {
                // Nếu không còn ảnh nào, gán ảnh mặc định
                existingProduct.setThumbnail(null);
            }


            return productRepository.save(existingProduct);
        }
        return null;
    }

    @Override
    @Transactional
    public void deleteProduct(long id) {
        Optional<Product> optionalProduct= productRepository.findById(id);
        optionalProduct.ifPresent(productRepository::delete);

    }

    @Override
    public Boolean exitsByName(String name) {
        return productRepository.existsByName(name);
    }

    @Override
    @Transactional
    public ProductImage createProductImage(
            Long productId, ProductImageDTO productImageDTO) throws Exception {

        Product existingProduct = productRepository
                .findById(productId)
                .orElseThrow(()-> new DataNotFoundException("Cannot find product with id : "+ productImageDTO.getProductId()));

        ProductImage newProductImage = ProductImage
                .builder()
                .product(existingProduct)
                .imageUrl(productImageDTO.getImageUrl())
                .build();
        //Không cho insert quá 5 ảnh cho 1 sản phẩm

        int size =  productImageRepository.findByProductId(productId).size();
        if (size >= ProductImage.MAXIMUM_IMAGE_PER_PRODUCT){
            throw  new InvalidParamException("number of imges must be <= 5");
        }
        return productImageRepository.save(newProductImage);
    }



}
