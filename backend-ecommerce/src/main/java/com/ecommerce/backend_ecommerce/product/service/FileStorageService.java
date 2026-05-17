package com.ecommerce.backend_ecommerce.product.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024L; // 5MB

    private final Path uploadDir;

    public FileStorageService(@Value("${file.upload-dir:./uploads}") String uploadDirStr) {
        this.uploadDir = Paths.get(uploadDirStr, "products").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
            log.info("file_storage_dir={}", this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create upload directory: " + this.uploadDir, e);
        }
    }

    /**
     * Lưu file upload vào local disk.
     * @return URL path để truy cập: /api/files/products/{filename}
     */
    public String storeFile(MultipartFile file) {
        validateFile(file);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getExtension(originalFilename);
        String filename = UUID.randomUUID() + "." + extension;

        try {
            Path targetPath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("file_stored filename={} size={}", filename, file.getSize());
            return "/api/files/products/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + filename, e);
        }
    }

    /**
     * Xoá file khỏi disk theo URL path.
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;
        String filename = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
        try {
            Path filePath = uploadDir.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
            log.info("file_deleted filename={}", filename);
        } catch (IOException e) {
            log.warn("file_delete_failed url={}", fileUrl, e);
        }
    }

    /**
     * Trả về đường dẫn tuyệt đối của file để serve.
     */
    public Path getFilePath(String filename) {
        return uploadDir.resolve(filename).normalize();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Only JPEG, PNG, WebP, GIF images are allowed");
        }
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) return "jpg";
        return filename.substring(dotIndex + 1).toLowerCase();
    }
}
