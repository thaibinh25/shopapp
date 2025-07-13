package com.project.shopapp.controller;

import com.github.javafaker.Faker;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.*;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;

import com.project.shopapp.response.ProductListResponse;
import com.project.shopapp.response.ProductResponse;
import com.project.shopapp.response.ResponseObject;
import com.project.shopapp.services.ProductService;
import com.project.shopapp.services.S3Service;
import com.project.shopapp.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.io.InputStream;



import static org.springframework.http.MediaType.parseMediaType;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
    private static final org.slf4j.Logger logger =  LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;
    private final LocalizationUtils localizationUtils;
    private final ModelMapper modelMapper;
    private final S3Service s3Service;

    @GetMapping
    public ResponseEntity<ProductListResponse> getProducts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0",name = "category_id") Long categoryId,
            @RequestParam(defaultValue = "0",name = "brand_id") Long brandId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Float minRating,
            @RequestParam(required = false) String badge,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "15") int limit
    ) {

        //tạo pageable từ thôgn tin trang và giới hạn
        PageRequest pageRequest = PageRequest.of(
                page, limit,
                //Sort.by("createdAt").descending());
                Sort.by("id").ascending());

        Page<ProductResponse> productPage = productService.getAllProducts(keyword,categoryId,brandId,minPrice,maxPrice,minRating, badge,pageRequest);
        //Lấy tổng số trang
        int totalPage = productPage.getTotalPages();
        List<ProductResponse> products = productPage.getContent();
        return ResponseEntity.ok(ProductListResponse
                .builder()
                .products(products)
                .totalPage(totalPage)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(
            @PathVariable("id") Long productId
    ) {
        try {
            Product existingProduct = productService.getProductById(productId);
            return ResponseEntity.ok(ProductResponse.fromProduct(existingProduct));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @GetMapping("/by-ids")
    public ResponseEntity<?> getProductsByIds(@RequestParam("ids") String ids) {
        //eg: 1,3,5,7
        try {
            // Tách chuỗi ids thành một mảng các số nguyên
            List<Long> productIds = Arrays.stream(ids.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            List<ProductResponse> products = productService.findProductsByIds(productIds);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "")
    public ResponseEntity<?> createProduct(
            @Valid @RequestBody ProductDTO productDTO,
            BindingResult result
    ){
        try{
            if (result.hasErrors()){
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessages);
            }

            Product newProduct = productService.createProduct(productDTO);


            return ResponseEntity.ok(newProduct);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @PostMapping(value = "/uploads/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImages(
            @PathVariable("id") Long productId,
            @ModelAttribute("files") List<MultipartFile> files
    ){
        try{
            Product existingProduct = productService.getProductById(productId);
            List<ProductImage> productImages = new ArrayList<>();
            files = files == null ? new ArrayList<MultipartFile>() : files;
            if (files.size()> ProductImage.MAXIMUM_IMAGE_PER_PRODUCT){
                return ResponseEntity.badRequest().body(localizationUtils.getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_MAX_5));
            }
            for (MultipartFile file : files){

                if (file.getSize() == 0){
                    continue;
                }
                //Kiểm tra kích thước file ảnh và định dạng
                if (file.getSize()> 10*1024*1024){// kích thước >10MB
                    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                            .body(localizationUtils.getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_FILE_LARGE));
                }
                String contentType = file.getContentType();
                if (contentType ==null || !contentType.startsWith("image/")){
                    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                            .body(localizationUtils.getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_FILE_MUST_BE_IMAGE));
                }
                //Lưu file và cập nhật thumbnail trong DTO
               // String filename = storeFile(file);//Thay thế hàm này với code của bạn để lưu file
                //Lưu vào đối tượng product trong DB => sẽ làm sau
                //lưu vào bảng product_images
                /*ProductImage productImage = productService.createProductImage(
                        existingProduct.getId(),
                        ProductImageDTO.builder()
                                .imageUrl(filename)
                                .build());*/
                // UPLOAD LÊN S3
                String imageUrl = s3Service.uploadFile(file);

                // TẠO BẢN GHI ẢNH VÀO DB
                ProductImage productImage = productService.createProductImage(
                        existingProduct.getId(),
                        ProductImageDTO.builder()
                                .imageUrl(imageUrl)
                                .build());

                /*if (productImages.isEmpty() &&
                        (existingProduct.getThumbnail() == null)||
                            existingProduct.getThumbnail().trim().isEmpty() ) { // Ảnh đầu tiên

                    existingProduct.setThumbnail(filename);
                    productService.save(existingProduct);
                }
                productImages.add(productImage);

            }*/
                // Nếu là ảnh đầu tiên & chưa có thumbnail, đặt thumbnail cho sản phẩm
                if (productImages.isEmpty() &&
                        (existingProduct.getThumbnail() == null || existingProduct.getThumbnail().trim().isEmpty())) {
                    existingProduct.setThumbnail(imageUrl);
                    productService.save(existingProduct);
                }

                productImages.add(productImage);
            }

            return ResponseEntity.ok().body(productImages);

        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }


    }



    @GetMapping("/images/{imageName:.+}")
    public ResponseEntity<StreamingResponseBody> streamImage(@PathVariable String imageName) {
        try {
            ClassPathResource imgFile = new ClassPathResource("static/images/" + imageName);

            if (!imgFile.exists()) {
                imgFile = new ClassPathResource("static/images/notfound.jpeg"); // ảnh mặc định nếu không có
            }

            InputStream inputStream = imgFile.getInputStream();
            String contentType = URLConnection.guessContentTypeFromStream(inputStream);
            if (contentType == null) contentType = "image/jpeg";

            StreamingResponseBody stream = outputStream -> {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
            };

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.set("Content-Disposition", "inline; filename=\"" + imageName + "\"");

            return new ResponseEntity<>(stream, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }



    @PutMapping("/{id}")
    public  ResponseEntity<?> updateProduct(
            @PathVariable long id,
            @RequestBody @Valid ProductDTO productDTO
    ){
        try{
            Product updateProduct = productService.updateProduct(id, productDTO);
            return ResponseEntity.ok(modelMapper.map(updateProduct, ProductResponse.class));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @DeleteMapping("/{id}")
    public  ResponseEntity<String> deleteProduct(@PathVariable long id){
        try{
            productService.deleteProduct(id);
            return ResponseEntity.ok(String.format("Product with id = %d deleted successfully",id));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }


    }

    // mở lên khi cần dùng thôi
    //@PostMapping("/generateFakeProducts")
    public ResponseEntity<String> generateFakeProducts(){
        Faker faker = new Faker();
        for (int i =0 ; i< 1000; i++){
            String productName = faker.commerce().productName();
            if (productService.exitsByName(productName)){
                continue;
            }
            ProductDTO productDTO = ProductDTO.builder()
                    .name(productName)
                    .price((float)faker.number().numberBetween(10,90000000))
                    .thumbnail("")
                    .description(faker.lorem().sentence())
                    .categoryId((long)faker.number().numberBetween(1,5))
                    .build();
            try{
                productService.createProduct(productDTO);
            }catch (Exception e){
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
        return ResponseEntity.ok("Fake Products created successfully");
    }

    @GetMapping("/badges")
    public ResponseEntity<List<String>> getAvailableBadges() {
        List<String> badges = productService.getDistinctBadges();
        return ResponseEntity.ok(badges);
    }
/*
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
*/

}

