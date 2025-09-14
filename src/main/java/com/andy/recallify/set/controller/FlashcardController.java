package com.andy.recallify.set.controller;

import com.andy.recallify.set.dto.FlashcardDto;
import com.andy.recallify.set.dto.McqDto;
import com.andy.recallify.set.dto.UpdateSRSRequest;
import com.andy.recallify.set.service.FlashcardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path="api/flashcard")
public class FlashcardController {
    private final FlashcardService flashcardService;

    @Autowired
    public FlashcardController(FlashcardService flashcardService, FlashcardService flashcardService1) {
        this.flashcardService = flashcardService;
    }

    @PostMapping(
            value = "/generate-from-pdf",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> generateFlashcardsFromPdf(
            @RequestParam("setId") Long id,
            @RequestParam("count") Long count,
            @RequestPart("file") MultipartFile file
    ) {
        try {
            flashcardService.generateAndSaveFlashcardsFromPdf(id, file, count);
            return ResponseEntity.ok(Map.of("setId", id));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to generate flashcards: " + e.getMessage());
        }
    }

    @GetMapping("/get/{setId}")
    public ResponseEntity<?> getFlashcards(@PathVariable Long setId) {
        try {
            List<FlashcardDto> flashcards = flashcardService.getFlashcards(setId);
            System.out.println(flashcards);
            return ResponseEntity.ok(flashcards);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to get flashcards: " + e.getMessage());
        }
    }

    @PostMapping("/SRS/update/{setId}")
    public ResponseEntity<?> updateFlashcardSRS(@RequestBody UpdateSRSRequest updateSRSRequest, @PathVariable Long setId) {
        try {
            System.out.println(updateSRSRequest);
            flashcardService.updateFlashcardSRS(setId, updateSRSRequest.grade(), updateSRSRequest.interval_hours(),
                                                updateSRSRequest.ef(), updateSRSRequest.repetitions());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return  ResponseEntity.internalServerError().body("Failed to update flashcards: " + e.getMessage());
        }
    }

}
