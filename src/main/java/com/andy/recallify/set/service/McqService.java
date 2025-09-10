package com.andy.recallify.set.service;

import com.andy.recallify.generation.GeminiService;
import com.andy.recallify.generation.PdfUploadService;
import com.andy.recallify.set.model.Mcq;
import com.andy.recallify.set.model.McqSRS;
import com.andy.recallify.set.model.Set;
import com.andy.recallify.set.dto.McqDto;
import com.andy.recallify.set.dto.McqParser;
import com.andy.recallify.set.dto.OptionDto;
import com.andy.recallify.set.repository.McqRepository;
import com.andy.recallify.set.repository.McqSRSRepository;
import com.andy.recallify.set.repository.SetRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    public void generateAndSaveMcqsFromPdf(Long mcqSetId, MultipartFile file, Long count) throws Exception {
        Set set = setRepository.findById(mcqSetId)
                .orElseThrow(() -> new IllegalArgumentException("Set not found"));

        String extractedText = pdfUploadService.extractTextFromPdf(file);

        String rawJson = geminiService.generateMcqs(extractedText, count);

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
        return new McqDto(m.getId(), m.getQuestion(), "", options);
    }
}
