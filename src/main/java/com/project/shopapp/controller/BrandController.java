package com.project.shopapp.controller;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.BrandDTO;
import com.project.shopapp.models.Brand;
import com.project.shopapp.repositories.BrandRepository;
import com.project.shopapp.response.UpdateBrandResponse;
import com.project.shopapp.services.BrandService;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/brands")
@RequiredArgsConstructor

public class BrandController {
    private final BrandService brandService;
    private final LocalizationUtils localizationUtils;
    private final BrandRepository brandRepository;

    @GetMapping("") //http://localhost:8088/api/v1/brands?page=1&limit=10
    public ResponseEntity<List<Brand>> getAllBrands(
            @RequestParam("page")   int page,
            @RequestParam("limit")  int limit
    ){
        List<Brand> brands = brandService.getAllBrands();
        return ResponseEntity.ok(brands);
    }

    @GetMapping("/category/{category_id}")
    public ResponseEntity<List<Brand>> getBrandsByCategory(@PathVariable(name = "category_id", required = false) Long categoryId) {
        List<Brand> brands;
        if (categoryId == null || categoryId == 0) {
            brands = brandRepository.findAll(); // tất cả brands
        } else {
            brands = brandRepository.findBrandsByCategoryId(categoryId); // chỉ brand theo category
        }
        List<Brand> result = brands.stream()
                .map(b -> Brand.builder().id(b.getId()).name(b.getName()).build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("")
    public ResponseEntity<?> creatBrand(@Valid @RequestBody BrandDTO brandDTO,
                                           BindingResult result){
        if (result.hasErrors()){
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(errorMessages);
        }
        brandService.createBrand(brandDTO);
        return ResponseEntity.ok(localizationUtils.getLocalizedMessage(MessageKeys.INSERT_BRAND_SUCCESSFULLY));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdateBrandResponse> updateBrand(
            @PathVariable Long id,
            @RequestBody BrandDTO brandDTO){
        brandService.updateBrand(id,brandDTO);
        return ResponseEntity.ok(UpdateBrandResponse.builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.UPDATE_BRAND_SUCCESSFULLY))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBrand(@PathVariable Long id){
        brandService.deleteBrand(id);
        return ResponseEntity.ok(localizationUtils.getLocalizedMessage(MessageKeys.DELETE_BRAND_SUCCESSFULLY));
    }

}
