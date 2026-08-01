package com.talentiq.service.impl;

import com.talentiq.exception.BadRequestException;
import com.talentiq.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path rootPath;

    public FileStorageServiceImpl(@Value("${app.storage.path}") String storagePath) {
        this.rootPath = Paths.get(storagePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootPath);
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize storage directory: " + rootPath, e);
        }
    }

    @Override
    public String store(MultipartFile file, Long userId, String fileName) {
        try {
            Path userDir = rootPath.resolve(String.valueOf(userId));
            Files.createDirectories(userDir);
            Path target = userDir.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString();
        } catch (IOException e) {
            throw new BadRequestException("Failed to store uploaded file: " + e.getMessage());
        }
    }
}
