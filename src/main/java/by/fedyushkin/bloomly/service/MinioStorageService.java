package by.fedyushkin.bloomly.service;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MinioStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private final MinioClient minioClient;
    private final String bucket;
    private final String publicUrl;
    private final String endpoint;

    public MinioStorageService(
            MinioClient minioClient,
            @Value("${minio.bucket}") String bucket,
            @Value("${minio.url}") String endpoint,
            @Value("${minio.public-url}") String publicUrl
    ) {
        this.minioClient = minioClient;
        this.bucket = bucket;
        this.endpoint = trimSlash(endpoint);
        this.publicUrl = trimSlash(publicUrl);
    }

    @PostConstruct
    public void ensureBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize MinIO bucket: " + bucket, e);
        }
    }

    public String uploadReviewPhoto(Long sessionId, MultipartFile file) {
        return uploadPhoto("reviews", sessionId, file);
    }

    public String uploadPortfolioPhoto(Long masterId, MultipartFile file) {
        return uploadPhoto("portfolios", masterId, file);
    }

    public String uploadProfilePhoto(Long userId, MultipartFile file) {
        return uploadPhoto("users", userId, file);
    }

    private String uploadPhoto(String folder, Long ownerId, MultipartFile file) {
        validateImage(file);
        String objectKey = folder + "/" + ownerId + "/" + UUID.randomUUID() + extension(file.getOriginalFilename());
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return objectKey;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload photo to MinIO", e);
        }
    }

    public void delete(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete photo from MinIO", e);
        }
    }

    public String presignedUrl(String objectKey) {
        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(1, TimeUnit.HOURS)
                            .build()
            );
            if (!publicUrl.equals(endpoint)) {
                return url.replace(endpoint, publicUrl);
            }
            return url;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate photo URL", e);
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Photo file is empty");
        }
//        String contentType = file.getContentType();
//        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only jpeg, png, webp and gif images are allowed");
//        }
    }

    private String extension(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0) {
            return "";
        }
        String ext = filename.substring(dot).toLowerCase(Locale.ROOT);
        if (!ext.matches("\\.(jpg|jpeg|png|webp|gif)")) {
            return "";
        }
        return ext;
    }

    private String trimSlash(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }
}
