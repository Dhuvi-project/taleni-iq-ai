package com.talentiq.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /** Stores the file under {@code {storagePath}/{userId}/{filename}} and returns the stored path. */
    String store(MultipartFile file, Long userId, String fileName);
}
