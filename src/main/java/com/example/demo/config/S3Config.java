package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// v1: 기존 AmazonS3 (Get/Put 등에서 사용)
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

// v2: Presigner (프리사인 URL 발급)
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    @Value("${cloud.aws.credentials.access-key:}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key:}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    // v1 - AmazonS3 클라이언트 (기존 로직 호환)
    @Bean
    public AmazonS3 amazonS3Client() {
        BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(creds))
                .build();
    }

    // v2 - S3Presigner (PresignController에서 사용)
    @Bean
    public S3Presigner s3Presigner() {
        Region r = Region.of(region);

        // 키를 yml에 두지 않고 IAM Role/환경변수로 주입할 때
        if (accessKey == null || accessKey.isBlank() || secretKey == null || secretKey.isBlank()) {
            return S3Presigner.builder()
                    .region(r)
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
        }

        return S3Presigner.builder()
                .region(r)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .build();
    }
}
