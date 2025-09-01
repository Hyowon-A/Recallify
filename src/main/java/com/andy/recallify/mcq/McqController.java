package com.andy.recallify.mcq;

import com.andy.recallify.generation.GeminiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping(path="api/mcq")
public class McqController {
    private final McqService mcqService;

    @Autowired
    public McqController(McqService mcqService) {
        this.mcqService = mcqService;
    }

    @PostMapping(
            value = "/generate-from-pdf",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> generateMcqsFromPdf(
            @RequestParam("setId") Long id,
            @RequestParam("count") Long count,
            @RequestPart("file") MultipartFile file
    ) {
        try {
            mcqService.generateAndSaveMcqsFromPdf(id, file, count);
            return ResponseEntity.ok(Map.of("setId", id));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to generate MCQs: " + e.getMessage());
        }
    }
}
