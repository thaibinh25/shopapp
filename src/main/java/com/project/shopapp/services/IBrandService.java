package com.project.shopapp.services;

import com.project.shopapp.dtos.BrandDTO;
import com.project.shopapp.models.Brand;

import java.util.List;

public interface IBrandService {
    Brand createBrand(BrandDTO brandDTO);

    Brand getBrandById(long id);

    List<Brand> getAllBrands();

    Brand updateBrand(long brandId, BrandDTO brandDTO);

    void deleteBrand(long id);
}
