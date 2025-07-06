package com.project.shopapp.configurations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AWSconfig {
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.AP_SOUTHEAST_2) // ví dụ Tokyo
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
    }
}
