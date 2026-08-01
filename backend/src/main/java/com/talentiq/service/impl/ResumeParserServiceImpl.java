package com.talentiq.service.impl;

import com.talentiq.exception.BadRequestException;
import com.talentiq.service.ResumeParserService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

@Service
public class ResumeParserServiceImpl implements ResumeParserService {

    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;

    @Override
    public String extractText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No file was uploaded");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("File exceeds the maximum allowed size of 10MB");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            throw new BadRequestException("Uploaded file must have a valid extension (.pdf or .docx)");
        }
        String extension = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);

        try (InputStream inputStream = file.getInputStream()) {
            return switch (extension) {
                case "pdf" -> extractFromPdf(inputStream);
                case "docx" -> extractFromDocx(inputStream);
                default -> throw new BadRequestException("Unsupported file type: ." + extension
                        + ". Only .pdf and .docx are supported.");
            };
        } catch (IOException e) {
            throw new BadRequestException("Failed to read uploaded file: " + e.getMessage());
        }
    }

    private String extractFromPdf(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            if (document.isEncrypted()) {
                throw new BadRequestException("Encrypted PDF files are not supported");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractFromDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }
}
