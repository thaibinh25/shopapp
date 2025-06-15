package com.project.shopapp.services;

import com.project.shopapp.dtos.BrandDTO;
import com.project.shopapp.models.Brand;
import com.project.shopapp.models.Category;
import com.project.shopapp.repositories.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService implements IBrandService{
    private final BrandRepository brandRepository;


    @Override
    @Transactional
    public Brand createBrand(BrandDTO brandDTO) {
        Brand newBrand = Brand.builder()
                .name(brandDTO.getName())
                .build();
        return brandRepository.save(newBrand);
    }

    @Override
    public Brand getBrandById(long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found"));
    }

    @Override
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    @Override
    @Transactional
    public Brand updateBrand(long brandId, BrandDTO brandDTO) {
        Brand existingBrand = getBrandById(brandId);
        existingBrand.setName(brandDTO.getName());
        brandRepository.save(existingBrand);
        return existingBrand;
    }

    @Override
    public void deleteBrand(long id) {
        brandRepository.deleteById(id);
    }
}
