package com.andy.recallify.generation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class PdfUploadController {

    private final PdfUploadService pdfUploadService;

    @Autowired
    public PdfUploadController(PdfUploadService pdfUploadService) {
        this.pdfUploadService = pdfUploadService;
    }

    @PostMapping("/pdf")
    public ResponseEntity<String> uploadPdf(@RequestParam("file") MultipartFile file) {
        String extractedText = pdfUploadService.extractTextFromPdf(file);
        return ResponseEntity.ok(extractedText);
    }
}
