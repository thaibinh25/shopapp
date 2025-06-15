package com.project.shopapp.repositories;
import com.project.shopapp.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product,Long> {

    boolean existsByName(String name);


    Page<Product> findAll(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
            "(:categoryId IS NULL OR :categoryId = 0 OR p.category.id = :categoryId) " +
            "AND (:brandId IS NULL OR :brandId = 0 OR p.brand.id = :brandId) " +
            "AND (:keyword IS NULL OR :keyword = '' " +
            "   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> searchProducts
            (@Param("keyword") String keyword,
             @Param("categoryId") Long categoryId,
             @Param("brandId") Long brandId,
             @Param("minPrice") Double minPrice,
             @Param("maxPrice") Double maxPrice,
             Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.productImages WHERE p.id = :productId")
    Optional<Product> getDetailProduct(@Param("productId") Long productId);

    @Query("SELECT p FROM Product p WHERE p.id IN :productIds")
    List<Product> findProductsByIds(@Param("productIds") List<Long> productIds);
}