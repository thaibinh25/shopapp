package com.project.shopapp.services;

import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.models.ProductImage;

public interface IproductImageService {

    ProductImage getById(Long id);
    void deleteImage(Long id) throws DataNotFoundException;
}
