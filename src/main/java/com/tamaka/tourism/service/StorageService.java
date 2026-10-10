package com.tamaka.tourism.service;

import com.tamaka.tourism.dto.UploadResponse;
import com.tamaka.tourism.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {
    @Value("${storage.r2.account-id:}")
    private String accountId;
    @Value("${storage.r2.access-key-id:}")
    private String accessKeyId;
    @Value("${storage.r2.secret-access-key:}")
    private String secretAccessKey;
    @Value("${storage.r2.bucket:}")
    private String bucket;
    @Value("${storage.r2.endpoint:}")
    private String endpoint;
    @Value("${storage.r2.public-url:}")
    private String publicUrl;

    public UploadResponse upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File is required");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException("File exceeds the 10 MB limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BusinessException("Only image files are supported");
        }
        if (accessKeyId.isBlank() || secretAccessKey.isBlank() || bucket.isBlank() || endpoint.isBlank() || publicUrl.isBlank()) {
            throw new BusinessException("Image storage is not configured");
        }

        String safeFolder = folder == null || folder.isBlank() ? "uploads" : folder
                .replaceAll("[^a-zA-Z0-9/_-]", "")
                .replaceAll("/{2,}", "/")
                .replaceAll("^/|/$", "");
        String extension = extension(file.getOriginalFilename(), contentType);
        String key = safeFolder + "/" + LocalDate.now() + "/" + UUID.randomUUID() + extension;

        try (S3Client client = client()) {
            client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .contentLength(file.getSize())
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );
        } catch (IOException | RuntimeException error) {
            throw new BusinessException("Could not upload image");
        }
        return new UploadResponse(key, publicUrl.replaceAll("/$", "") + "/" + key, contentType, file.getSize());
    }

    private S3Client client() {
        String resolvedEndpoint = endpoint.isBlank()
                ? "https://" + accountId + ".r2.cloudflarestorage.com"
                : endpoint;
        return S3Client.builder()
                .endpointOverride(URI.create(resolvedEndpoint))
                .region(Region.of("auto"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .build();
    }

    private String extension(String filename, String contentType) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9.]", "");
        }
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
    }
}
