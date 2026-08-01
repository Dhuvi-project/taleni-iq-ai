package com.talentiq.service;

import org.springframework.web.multipart.MultipartFile;

public interface ResumeParserService {
    /** Extracts raw text from a PDF or DOCX multipart file. Rejects unsupported types. */
    String extractText(MultipartFile file);
}
