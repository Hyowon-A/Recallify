package com.andy.recallify.set.service;

import com.andy.recallify.generation.GeminiService;
import com.andy.recallify.generation.PdfUploadService;
import com.andy.recallify.set.dto.UpdateSRSRequest;
import com.andy.recallify.set.model.FlashcardSRS;
import com.andy.recallify.set.model.Mcq;
import com.andy.recallify.set.model.McqSRS;
import com.andy.recallify.set.model.Set;
import com.andy.recallify.set.dto.McqDto;
import com.andy.recallify.set.dto.McqParser;
import com.andy.recallify.set.dto.OptionDto;
import com.andy.recallify.set.repository.McqRepository;
import com.andy.recallify.set.repository.McqSRSRepository;
import com.andy.recallify.set.repository.SetRepository;
import com.andy.recallify.user.model.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class McqService {

    private final McqRepository mcqRepository;
    private final SetRepository setRepository;
    private final GeminiService geminiService;
    private final PdfUploadService pdfUploadService;
    private final McqParser mcqParser;
    private final McqSRSRepository mcqSRSRepository;

    @Autowired
    public McqService(McqRepository mcqRepository, SetRepository setRepository, GeminiService geminiService, PdfUploadService pdfUploadService, McqParser mcqParser, McqSRSRepository mcqSRSRepository) {
        this.mcqRepository = mcqRepository;
        this.setRepository = setRepository;
        this.geminiService = geminiService;
        this.pdfUploadService = pdfUploadService;
        this.mcqParser = mcqParser;
        this.mcqSRSRepository = mcqSRSRepository;
    }

    public void generateAndSaveMcqsFromPdf(Long mcqSetId, MultipartFile file, Long count, String level) throws Exception {
        Set set = setRepository.findById(mcqSetId)
                .orElseThrow(() -> new IllegalArgumentException("Set not found"));

        String extractedText = pdfUploadService.extractTextFromPdf(file);

        String rawJson = geminiService.generateMcqs(extractedText, count, level);

        List<Mcq> mcqs = mcqParser.parse(rawJson, set);  // attach set to each mcq

        mcqRepository.saveAll(mcqs);

        // Initialize SRS for each MCQ
        List<McqSRS> srsList = mcqs.stream()
                .map(mcq -> new McqSRS(
                        mcq,
                        0,
                        0,
                        (float) 2.5,
                        null,
                        null
                ))
                .toList();

        mcqSRSRepository.saveAll(srsList);
    }

    public List<McqDto> getMcqs(Long setId) {
        return mcqRepository.findBySetId(setId).stream()
                .map(this::toDto)
                .toList();
    }

    private McqDto toDto(Mcq m) {
        int ans = m.getAnswer(); // 1..4
        List<OptionDto> options = List.of(
                new OptionDto("A", m.getOption1(), ans == 1, m.getExplanation1()),
                new OptionDto("B", m.getOption2(), ans == 2, m.getExplanation2()),
                new OptionDto("C", m.getOption3(), ans == 3, m.getExplanation3()),
                new OptionDto("D", m.getOption4(), ans == 4, m.getExplanation4())
        );

        McqSRS mcqSRS = mcqSRSRepository.findByMcqId(m.getId())
                .orElseThrow(() -> new IllegalArgumentException("MCQ not found"));

        LocalDateTime now = LocalDateTime.now();
        Timestamp nowTs = Timestamp.valueOf(now);

        String srsType = classify(mcqSRS, nowTs);

        return new McqDto(m.getId(), m.getQuestion(), "", options,
                            mcqSRS.getInterval_hours(), mcqSRS.getEf(), mcqSRS.getRepetitions(), srsType);
    }

    @Transactional
    public void updateMcqSRS(Long setId, int grade, float interval_hours, float ef, int repetitions) {
        McqSRS mSRS = mcqSRSRepository.findByMcqId(setId)
                .orElseThrow(() -> new IllegalArgumentException("Flashcard not found"));

        UpdateSRSRequest results = estimateNextInterval(grade, interval_hours, ef, repetitions);

        mSRS.setInterval_hours(results.interval_hours());
        mSRS.setEf(results.ef());
        mSRS.setRepetitions(results.repetitions());
        mSRS.setLastReviewedAt(Timestamp.valueOf(LocalDateTime.now()));

        long minutesToAdd = Math.round(results.interval_hours() * 60);
        LocalDateTime nextReviewAt = LocalDateTime.now().plusMinutes(minutesToAdd);
        mSRS.setNextReviewAt(Timestamp.valueOf(nextReviewAt));
        mcqSRSRepository.save(mSRS);
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

    private String classify(McqSRS srs,  Timestamp nowTs) {
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
