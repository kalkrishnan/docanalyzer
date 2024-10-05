package com.kkrishnan.docanalyzer;

import com.kkrishnan.docanalyzer.models.DocumentContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.textract.model.*;

import java.util.*;


@RestController
@RequestMapping("/api/document")
public class DocAnalyzerController {
    @Autowired
    DocumentRepositoryClient docRepositoryClient;

    @Autowired
    private AmazonTextractService textractService;

    @Autowired
    private DocAnalyzerService docAnalyzerService;

    @Autowired
    private LLMService llmService;

    @PostMapping("/analyze")
    public ResponseEntity analyzeDocument(@RequestParam("file") MultipartFile file) {
            docRepositoryClient.uploadDocument(file);
                 // Start async document analysis
            StartDocumentAnalysisResponse result = textractService.analyzeDocument(file);

            return ResponseEntity.ok(Map.of(
                    "jobId", result.jobId(),
                    "message", "Analysis started. Use /analyze/{jobId} to get results."
            ));

    }

    @GetMapping("/analyze/{jobId}")
    public ResponseEntity<?> getAnalysisResults(@PathVariable String jobId) {
        try {
            DocumentContent result =docAnalyzerService.getAnalysisResults(jobId);
            String dates = llmService.extractDates(result.extractedText());
            return ResponseEntity.ok(dates.substring(0, 500));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving analysis results: " + e.getMessage());
        }
    }



}
