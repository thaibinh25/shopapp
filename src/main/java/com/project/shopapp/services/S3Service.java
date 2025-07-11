package com.project.shopapp.services;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.text.Normalizer;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;


    public String uploadFile(MultipartFile file) throws IOException {

        // 1. Lấy tên file gốc
        String originalFileName = file.getOriginalFilename();

        // 2. Chuẩn hóa tên file: xóa dấu tiếng Việt, khoảng trắng, kí tự đặc biệt
        String normalizedFileName = normalizeFileName(originalFileName);


        String key = "products/" + UUID.randomUUID() + "_" + normalizedFileName;
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                //.acl("public-read")
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, software.amazon.awssdk.core.sync.RequestBody.fromInputStream(
                file.getInputStream(), file.getSize()));

        return "https://" + bucket + ".s3.amazonaws.com/" + key;
    }

    // Hàm chuẩn hóa tên file
    private String normalizeFileName(String fileName) {
        String normalized = Normalizer.normalize(fileName, Normalizer.Form.NFD);
        String withoutDiacritics = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return withoutDiacritics.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

}
