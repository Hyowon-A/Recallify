package com.andy.recallify.mcq;

import com.andy.recallify.generation.GeminiService;
import com.andy.recallify.generation.PdfUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class McqService {

    private final McqRepository mcqRepository;
    private final McqSetRepository mcqSetRepository;
    private final GeminiService geminiService;
    private final PdfUploadService pdfUploadService;
    private final McqParser mcqParser;

    @Autowired
    public McqService(McqRepository mcqRepository, McqSetRepository mcqSetRepository, GeminiService geminiService, PdfUploadService pdfUploadService, McqParser mcqParser) {
        this.mcqRepository = mcqRepository;
        this.mcqSetRepository = mcqSetRepository;
        this.geminiService = geminiService;
        this.pdfUploadService = pdfUploadService;
        this.mcqParser = mcqParser;
    }

    public void generateAndSaveMcqsFromPdf(Long mcqSetId, MultipartFile file) throws Exception {
        McqSet set = mcqSetRepository.findById(mcqSetId)
                .orElseThrow(() -> new IllegalArgumentException("Set not found"));

        String extractedText = pdfUploadService.extractTextFromPdf(file);

        String rawJson = geminiService.generateMcqs(extractedText);

        List<Mcq> mcqs = mcqParser.parse(rawJson, set);  // attach set to each mcq

        mcqRepository.saveAll(mcqs);
    }
}
