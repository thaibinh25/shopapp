package com.project.shopapp.configurations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class AWSconfig {
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.AP_NORTHEAST_1) // ví dụ Tokyo
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("ACCESS_KEY", "SECRET_KEY")))
                .build();
    }
}
