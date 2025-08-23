package com.andy.recallify.mcq;

import com.andy.recallify.generation.GeminiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path="api/mcq")
public class McqController {
    private final McqService mcqService;

    @Autowired
    public McqController(McqService mcqService) {
        this.mcqService = mcqService;
    }

    @PostMapping("/{id}/generate-from-pdf")
    public ResponseEntity<?> generateMcqsFromPdf(@PathVariable Long id, @RequestParam("file") MultipartFile file) {

        try {
            mcqService.generateAndSaveMcqsFromPdf(id, file);
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to generate MCQs: " + e.getMessage());
        }
    }
}
