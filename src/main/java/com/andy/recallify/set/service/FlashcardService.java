package com.andy.recallify.set.service;

import com.andy.recallify.generation.GeminiService;
import com.andy.recallify.generation.PdfUploadService;
import com.andy.recallify.set.dto.FlashcardDto;
import com.andy.recallify.set.dto.FlashcardParser;
import com.andy.recallify.set.dto.UpdateSRSRequest;
import com.andy.recallify.set.model.Flashcard;
import com.andy.recallify.set.model.FlashcardSRS;
import com.andy.recallify.set.model.McqSRS;
import com.andy.recallify.set.model.Set;
import com.andy.recallify.set.repository.FlashcardRepository;
import com.andy.recallify.set.repository.FlashcardSRSRepository;
import com.andy.recallify.set.repository.SetRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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

    public void generateAndSaveFlashcardsFromPdf(Long setId, MultipartFile file, Long count, String level) throws Exception {
        Set set = setRepository.findById(setId)
                .orElseThrow(() -> new IllegalArgumentException("Set not found"));

        String extractedText = pdfUploadService.extractTextFromPdf(file);

        String rawJson = geminiService.generateFlashcards(extractedText, count, level);

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
        FlashcardSRS flashcardSRS = flashcardSRSRepository.findByFlashcardId(flashcard.getId())
                .orElseThrow(() -> new IllegalArgumentException("Flashcard not found"));

        LocalDateTime now = LocalDateTime.now();
        Timestamp nowTs = Timestamp.valueOf(now);

        String srsType = classify(flashcardSRS, nowTs);

        return new FlashcardDto(flashcard.getId(), flashcard.getFront(), flashcard.getBack(),
                                flashcardSRS.getInterval_hours(), flashcardSRS.getEf(), flashcardSRS.getRepetitions(),
                                srsType);
    }

    @Transactional
    public void updateFlashcardSRS(Long setId, int grade, float interval_hours, float ef, int repetitions) {
        FlashcardSRS fSRS = flashcardSRSRepository.findByFlashcardId(setId)
                .orElseThrow(() -> new IllegalArgumentException("Flashcard not found"));

        UpdateSRSRequest results = estimateNextInterval(grade, interval_hours, ef, repetitions);

        fSRS.setInterval_hours(results.interval_hours());
        fSRS.setEf(results.ef());
        fSRS.setRepetitions(results.repetitions());
        fSRS.setLastReviewedAt(Timestamp.valueOf(LocalDateTime.now()));

        long minutesToAdd = Math.round(results.interval_hours() * 60);
        LocalDateTime nextReviewAt = LocalDateTime.now().plusMinutes(minutesToAdd);
        fSRS.setNextReviewAt(Timestamp.valueOf(nextReviewAt));
        flashcardSRSRepository.save(fSRS);
    }

    private UpdateSRSRequest estimateNextInterval(int grade, float interval_hours, float ef, int rep) {
        int nextReps = rep;
        double nextEf = ef;
        double nextInterval;

        if (grade < 2) {
            // Forgot
            nextInterval = (rep == 0) ? 1 : 24; // 1 hour or 1 day
            nextReps = 0;
            nextEf = Math.max(1.3, ef - 0.2);
        } else {
            // Remembered
            nextReps += 1;

            if (nextReps == 1) {
                if (grade == 2) nextInterval = 12;
                else if (grade == 3) nextInterval = 24;
                else nextInterval = 48; // grade == 4
            } else if (nextReps == 2) {
                if (grade == 2) nextInterval = 72;
                else if (grade == 3) nextInterval = 144;
                else nextInterval = 288; // grade == 4
            } else {
                nextInterval = interval_hours * nextEf;
                if (grade == 2) nextInterval *= 0.8;
                else if (grade == 4) nextInterval *= 1.3;
            }

            // Update EF if grade ≥ 3
            nextEf = ef + (0.1 - (5 - grade) * (0.08 + (5 - grade) * 0.02));
            nextEf = Math.max(1.3, nextEf);
        }

        nextInterval = Math.min(nextInterval, 1440); // Cap to 60 days

        return new UpdateSRSRequest(grade, (float) nextInterval, (float) nextEf, nextReps);
    }

    private String classify(FlashcardSRS srs, Timestamp nowTs) {
        String srsType = "";
        if (srs.getLastReviewedAt() == null) srsType = "newC";
        if (srs.getRepetitions() <= 2 && srs.getLastReviewedAt() != null) {
            srsType = "learn";
        }
        if (srs.getRepetitions() > 2 && srs.getNextReviewAt().before(nowTs)) {
            srsType = "due";
        }
        return srsType;
    }
}
