package com.andy.recallify.generation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/ai/")
public class GeminiController {

    private final GeminiService geminiService;

    @Autowired
    public GeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/generateMcqs")
    public ResponseEntity<?> generateMcqs(@RequestBody Map<String, String> request) {
        String inputText = request.get("text");
        Long count = Long.parseLong(request.get("count"));
        if (inputText == null || inputText.isBlank()) {
            return ResponseEntity.badRequest().body("Input text is required");
        }

        try {
            String jsonMcqs = geminiService.generateMcqs(inputText, count);
            return ResponseEntity.ok(Map.of("mcqs", jsonMcqs));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to generate MCQs: " + e.getMessage());
        }
    }
}
