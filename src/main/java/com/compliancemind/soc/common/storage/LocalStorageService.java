package com.compliancemind.soc.common.storage;

import com.compliancemind.soc.common.constants.SocConstants;
import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * 本地磁盘附件存储（合规请求、项目附件等），校验扩展名并生成相对路径。
 */
@Service
public class LocalStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        SocConstants.Storage.EXT_PDF,
        SocConstants.Storage.EXT_DOC,
        SocConstants.Storage.EXT_DOCX,
        SocConstants.Storage.EXT_XLS,
        SocConstants.Storage.EXT_XLSX,
        SocConstants.Storage.EXT_PNG,
        SocConstants.Storage.EXT_JPG,
        SocConstants.Storage.EXT_JPEG,
        SocConstants.Storage.EXT_WEBP,
        SocConstants.Storage.EXT_TXT,
        SocConstants.Storage.EXT_CSV);

    @Value("${app.storage.root}")
    private String storageRoot;

    public StoredFile storeRequestAttachment(Long requestId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(BizErrorCode.STORAGE_FILE_EMPTY);
        }
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new BizException(BizErrorCode.STORAGE_FILENAME_EMPTY);
        }
        String extension = extractExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BizException(BizErrorCode.STORAGE_FILE_TYPE_INVALID);
        }

        String relativePath = SocConstants.Storage.REQUEST_PATH_PREFIX + requestId + "/" + LocalDate.now() + "/" + UUID.randomUUID() + extension;
        Path target = Path.of(storageRoot).resolve(relativePath);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new BizException(BizErrorCode.STORAGE_SAVE_FAILED);
        }
        return new StoredFile(originalFilename, relativePath.replace('\\', '/'), file.getContentType(), file.getSize());
    }

    public StoredFile storeProjectAttachment(Long projectId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(BizErrorCode.STORAGE_FILE_EMPTY);
        }
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new BizException(BizErrorCode.STORAGE_FILENAME_EMPTY);
        }
        String extension = extractExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BizException(BizErrorCode.STORAGE_FILE_TYPE_INVALID);
        }

        String relativePath = SocConstants.Storage.PROJECT_PATH_PREFIX + projectId + "/" + LocalDate.now() + "/" + UUID.randomUUID() + extension;
        Path target = Path.of(storageRoot).resolve(relativePath);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new BizException(BizErrorCode.STORAGE_SAVE_FAILED);
        }
        return new StoredFile(originalFilename, relativePath.replace('\\', '/'), file.getContentType(), file.getSize());
    }

    private String extractExtension(String filename) {
        int index = filename.lastIndexOf('.');
        return index < 0 ? "" : filename.substring(index);
    }

    public record StoredFile(String originalFilename, String relativePath, String contentType, long fileSize) {
    }

    public StoredFile storeRequestMasterTemplateFile(Long requestMasterId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(BizErrorCode.STORAGE_FILE_EMPTY);
        }
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new BizException(BizErrorCode.STORAGE_FILENAME_EMPTY);
        }
        String extension = extractExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BizException(BizErrorCode.STORAGE_FILE_TYPE_INVALID);
        }
        String relativePath = SocConstants.Storage.REQUEST_MASTER_TEMPLATE_PATH_PREFIX
            + requestMasterId + "/" + LocalDate.now() + "/" + UUID.randomUUID() + extension;
        Path target = Path.of(storageRoot).resolve(relativePath);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new BizException(BizErrorCode.STORAGE_SAVE_FAILED);
        }
        return new StoredFile(originalFilename, relativePath.replace('\\', '/'), file.getContentType(), file.getSize());
    }

    public Path resolveAbsolutePath(String relativePath) {
        return Path.of(storageRoot).resolve(relativePath).normalize();
    }

    public byte[] readFileBytes(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            throw new BizException(BizErrorCode.STORAGE_SAVE_FAILED);
        }
        Path target = resolveAbsolutePath(relativePath);
        if (!Files.exists(target) || !Files.isRegularFile(target)) {
            throw new BizException(BizErrorCode.STORAGE_SAVE_FAILED);
        }
        try {
            return Files.readAllBytes(target);
        } catch (IOException exception) {
            throw new BizException(BizErrorCode.STORAGE_SAVE_FAILED);
        }
    }
}

