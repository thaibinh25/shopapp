package com.project.shopapp.controller;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.ProductImageDTO;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.repositories.ProductImageRepository;
import com.project.shopapp.services.ProductImageService;
import com.project.shopapp.services.ProductService;
import com.project.shopapp.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/productImages")
@RequiredArgsConstructor
public class ProductImageController {
    private final ProductImageService productImageService;
    private final ProductService productService;
    private final LocalizationUtils localizationUtils;

    @PostMapping(value = "uploads/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImages(
            @PathVariable("id") Long productId,
            @ModelAttribute("files") List<MultipartFile> files
    ) {
        try {
            Product existingProduct = productService.getProductById(productId);
            List<ProductImage> productImages = new ArrayList<>();
            files = files == null ? new ArrayList<>() : files;

            if ((existingProduct.getProductImages().size() + files.size()) > ProductImage.MAXIMUM_IMAGE_PER_PRODUCT) {
                return ResponseEntity.badRequest()
                        .body(localizationUtils.getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_MAX_5));
            }

            for (MultipartFile file : files) {
                if (file.getSize() == 0) continue;

                if (file.getSize() > 10 * 1024 * 1024) {
                    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                            .body(localizationUtils.getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_FILE_LARGE));
                }

                if (!file.getContentType().startsWith("image/")) {
                    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                            .body(localizationUtils.getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_FILE_MUST_BE_IMAGE));
                }

                String filename = storeFile(file);

                ProductImage productImage = productService.createProductImage(
                        existingProduct.getId(),
                        ProductImageDTO.builder().imageUrl(filename).build());

                if (existingProduct.getThumbnail() == null || existingProduct.getThumbnail().isEmpty()) {
                    existingProduct.setThumbnail(filename);
                    productService.save(existingProduct);
                }

                productImages.add(productImage);
            }

            return ResponseEntity.ok().body(productImages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<?> deleteImage(@PathVariable("imageId") Long imageId) {
        try {
            ProductImage image = productImageService.getById(imageId);

            if (image != null) {
                Product product = image.getProduct();

                productImageService.deleteImage(imageId);


                // Optional: xoá file trên hệ thống nếu cần
                try {
                    deleteFileFromStorage(image.getImageUrl());
                } catch (Exception e) {
                    // Ghi log thôi, không throw để không ảnh hưởng tới API xoá
                    System.err.println("Không xoá được file: " + e.getMessage());
                }

                return ResponseEntity.ok("Image deleted");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not found");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }




    private String storeFile(MultipartFile file) throws IOException {
        if (!IsImageFile(file) || file.getOriginalFilename() == null){
            throw  new IOException("Invailid image format");
        }
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        //Thêm UUID vào trước ten file để đảm bảo tên file là duy nhất
        String uniqueFilename = UUID.randomUUID().toString()+ "_" + filename;
        //Đường dẫn đến thư mục mà bạn muốn lưu file
        java.nio.file.Path uploadDir = Paths.get("uploads");
        if (!Files.exists(uploadDir)){
            Files.createDirectories(uploadDir);
        }
        //Đường dẫn đầy đủ đến file
        java.nio.file.Path destination = Paths.get(uploadDir.toString(),uniqueFilename);
        //Sao chép file vào thư mục đích
        Files.copy(file.getInputStream(),destination, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFilename;
    }
    private boolean IsImageFile(MultipartFile file){
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    private void deleteFileFromStorage(String filename) {
        Path filePath = Paths.get("uploads").resolve(filename);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            e.printStackTrace(); // log lỗi nếu cần
        }
    }



}
