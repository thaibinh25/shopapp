package com.project.shopapp.repositories;

import com.project.shopapp.models.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand,Long> {

    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.category.id = :categoryId")
    List<Brand> findBrandsByCategoryId(@Param("categoryId") Long categoryId);

}
