package com.andy.recallify.set.service;

import com.andy.recallify.generation.GeminiService;
import com.andy.recallify.generation.PdfUploadService;
import com.andy.recallify.set.dto.FlashcardDto;
import com.andy.recallify.set.dto.FlashcardParser;
import com.andy.recallify.set.model.Flashcard;
import com.andy.recallify.set.model.FlashcardSRS;
import com.andy.recallify.set.model.McqSRS;
import com.andy.recallify.set.model.Set;
import com.andy.recallify.set.repository.FlashcardRepository;
import com.andy.recallify.set.repository.FlashcardSRSRepository;
import com.andy.recallify.set.repository.SetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final SetRepository setRepository;
    private final GeminiService geminiService;
    private final PdfUploadService pdfUploadService;
    private final FlashcardParser flashcardParser;
    private final FlashcardSRSRepository flashcardSRSRepository;

    @Autowired
    public FlashcardService(FlashcardRepository flashcardRepository, SetRepository setRepository, GeminiService geminiService, PdfUploadService pdfUploadService, FlashcardParser flashcardParser, FlashcardSRSRepository flashcardSRSRepository) {
        this.flashcardRepository = flashcardRepository;
        this.setRepository = setRepository;
        this.geminiService = geminiService;
        this.pdfUploadService = pdfUploadService;
        this.flashcardParser = flashcardParser;
        this.flashcardSRSRepository = flashcardSRSRepository;
    }

    public void generateAndSaveFlashcardsFromPdf(Long setId, MultipartFile file, Long count) throws Exception {
        Set set = setRepository.findById(setId)
                .orElseThrow(() -> new IllegalArgumentException("Set not found"));

        String extractedText = pdfUploadService.extractTextFromPdf(file);

        String rawJson = geminiService.generateFlashcards(extractedText, count);

        List<Flashcard> flashcards = flashcardParser.parse(rawJson, set);  // attach set to each flashcard

        flashcardRepository.saveAll(flashcards);

        // Initialize SRS for each MCQ
        List<FlashcardSRS> srsList = flashcards.stream()
                .map(flashcard -> new FlashcardSRS(
                        flashcard,
                        0,
                        0,
                        (float) 2.5,
                        null,
                        null
                ))
                .toList();

        flashcardSRSRepository.saveAll(srsList);
    }

    public List<FlashcardDto> getFlashcards(Long setId) {
        return flashcardRepository.findBySetId(setId).stream()
                .map(this::toFlashcard)
                .toList();
    }

    private FlashcardDto toFlashcard(Flashcard flashcard) {
        return new FlashcardDto(flashcard.getId(), flashcard.getFront(), flashcard.getBack());
    }
}
