package com.andy.recallify.mcq.service;

import com.andy.recallify.generation.GeminiService;
import com.andy.recallify.generation.PdfUploadService;
import com.andy.recallify.mcq.Mcq;
import com.andy.recallify.mcq.Set;
import com.andy.recallify.mcq.dto.McqDto;
import com.andy.recallify.mcq.dto.McqParser;
import com.andy.recallify.mcq.dto.OptionDto;
import com.andy.recallify.mcq.repository.McqRepository;
import com.andy.recallify.mcq.repository.SetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class McqService {

    private final McqRepository mcqRepository;
    private final SetRepository setRepository;
    private final GeminiService geminiService;
    private final PdfUploadService pdfUploadService;
    private final McqParser mcqParser;

    @Autowired
    public McqService(McqRepository mcqRepository, SetRepository setRepository, GeminiService geminiService, PdfUploadService pdfUploadService, McqParser mcqParser) {
        this.mcqRepository = mcqRepository;
        this.setRepository = setRepository;
        this.geminiService = geminiService;
        this.pdfUploadService = pdfUploadService;
        this.mcqParser = mcqParser;
    }

    public void generateAndSaveMcqsFromPdf(Long mcqSetId, MultipartFile file, Long count) throws Exception {
        Set set = setRepository.findById(mcqSetId)
                .orElseThrow(() -> new IllegalArgumentException("Set not found"));

        String extractedText = pdfUploadService.extractTextFromPdf(file);

        String rawJson = geminiService.generateMcqs(extractedText, count);

        List<Mcq> mcqs = mcqParser.parse(rawJson, set);  // attach set to each mcq

        mcqRepository.saveAll(mcqs);
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
