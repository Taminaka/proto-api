package com.tamaka.tourism.service;

import com.tamaka.tourism.dto.UploadResponse;
import com.tamaka.tourism.exception.BusinessException;
import com.tamaka.tourism.entity.Role;
import com.tamaka.tourism.entity.User;
import com.tamaka.tourism.entity.ImageUpload;
import com.tamaka.tourism.repository.ImageUploadRepository;
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
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
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
    @Value("${limits.images-per-user-per-day:50}")
    private int imageLimit;
    private final ImageUploadRepository imageUploadRepository;

    public UploadResponse upload(MultipartFile file, String folder) {
        return upload(file, folder, null);
    }

    public UploadResponse upload(MultipartFile file, String folder, User user) {
        validateFolder(folder, user);
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
        if (user != null && !user.getRoles().contains(Role.ADMIN)
                && imageUploadRepository.countByUserIdAndPurposeAndCreatedAtAfter(
                user.getId(), "CONTENT", LocalDateTime.now().minusHours(24)) >= imageLimit) {
            throw new BusinessException("Image upload limit reached: maximum " + imageLimit + " images every 24 hours");
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
        } catch (S3Exception error) {
            log.error("R2 upload failed: status={}, code={}, message={}",
                    error.statusCode(),
                    error.awsErrorDetails() == null ? "unknown" : error.awsErrorDetails().errorCode(),
                    error.awsErrorDetails() == null ? "unknown" : error.awsErrorDetails().errorMessage());
            throw new BusinessException("Could not upload image");
        } catch (IOException | RuntimeException error) {
            log.error("R2 upload failed: type={}, message={}",
                    error.getClass().getSimpleName(), error.getMessage());
            throw new BusinessException("Could not upload image");
        }
        if (user != null) {
            imageUploadRepository.save(ImageUpload.builder()
                    .user(user).objectKey(key).purpose("CONTENT").createdAt(LocalDateTime.now()).build());
        }
        return new UploadResponse(key, publicUrl.replaceAll("/$", "") + "/" + key, contentType, file.getSize());
    }

    public UploadResponse replaceAvatar(User user, MultipartFile file) {
        LocalDateTime windowStart = LocalDateTime.now().minusHours(24);
        long changes = imageUploadRepository.countByUserIdAndPurposeAndCreatedAtAfter(
                user.getId(), "AVATAR", windowStart);
        if (changes >= 3) {
            throw new BusinessException("Profile photo limit reached: maximum 3 changes every 24 hours");
        }

        UploadResponse uploaded = upload(file, "users/" + user.getId() + "/avatar", user);
        imageUploadRepository.findRecentByUserAndPurpose(
                        user.getId(), "AVATAR",
                        org.springframework.data.domain.PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .ifPresent(previous -> delete(previous.getObjectKey()));

        imageUploadRepository.save(ImageUpload.builder()
                .user(user)
                .objectKey(uploaded.key())
                .purpose("AVATAR")
                .createdAt(LocalDateTime.now())
                .build());
        return uploaded;
    }

    public void delete(String key) {
        if (key == null || key.isBlank() || accessKeyId.isBlank() || secretAccessKey.isBlank()
                || bucket.isBlank() || endpoint.isBlank()) {
            return;
        }
        try (S3Client client = client()) {
            client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (RuntimeException ignored) {
            // A stale object must not prevent the new image from being stored.
        }
    }

    private void validateFolder(String folder, User user) {
        if (user == null) {
            throw new BusinessException("Authentication is required");
        }
        String root = folder == null ? "" : folder.split("/")[0].toLowerCase(Locale.ROOT);
        boolean allowed = switch (root) {
            case "users" -> folder.startsWith("users/" + user.getId() + "/");
            case "attractions" -> hasRole(user, Role.GESTOR, Role.ADMIN);
            case "commerces", "services" -> hasRole(user, Role.OPERADOR, Role.ADMIN);
            case "events" -> hasRole(user, Role.GESTOR, Role.OPERADOR, Role.ADMIN);
            default -> false;
        };
        if (!allowed) {
            throw new BusinessException("You do not have permission to upload to this folder");
        }
    }

    private boolean hasRole(User user, Role... roles) {
        return java.util.Arrays.stream(roles).anyMatch(user.getRoles()::contains);
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
