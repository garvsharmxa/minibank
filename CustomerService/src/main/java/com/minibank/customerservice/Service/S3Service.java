package com.minibank.customerservice.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    @Qualifier("s3Client")
    private final S3Client s3Client;

    @Value("${aws.bucket.name}")
    private String bucketName;

    public String uploadFile(byte[] fileBytes, String originalFileName) {

        String fileKey = "kyc/" + UUID.randomUUID() + "_" + originalFileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .contentType("application/pdf") // Changed from image/jpeg
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));

        // Return S3 URL
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName, "ap-south-1", fileKey);
    }
}