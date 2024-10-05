package com.kkrishnan.docanalyzer;

import com.kkrishnan.docanalyzer.models.DocumentContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.services.textract.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.*;


@RestController
@RequestMapping("/api/document")
public class DocAnalyzer {
    @Autowired
    DocumentRepositoryClient docRepositoryClient;

    @Autowired
    private AmazonTextractService textractService;

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
            List<String> extractedText = new ArrayList<>();
            String nextToken = null;
            List<Map<String, Object>> extractedTables = new ArrayList<>();

            do {


                GetDocumentAnalysisResponse result = textractService.getDocumentAnalysisResults(jobId, nextToken);

                for (Block block : result.blocks()) {
                    if (block.blockType().equals(BlockType.LINE)) {
                        extractedText.add(block.text());
                    }
                }
                extractedTables.addAll(processTables(result.blocks()));

                nextToken = result.nextToken();
            } while (nextToken != null);

            return ResponseEntity.ok(new DocumentContent(null, null, extractedText,extractedTables));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving analysis results: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> processTables(List<Block> blocks) {
        List<Map<String, Object>> tables = new ArrayList<>();
        Map<String, Block> blockMap = new HashMap<>();
        blocks.forEach(block -> blockMap.put(block.id(), block));

        blocks.stream()
                .filter(block -> block.blockType().equals(BlockType.TABLE))
                .forEach(tableBlock -> {
                    Map<String, Object> tableData = new HashMap<>();
                    List<List<String>> tableContent = new ArrayList<>();

                    tableBlock.relationships().stream()
                            .filter(rel -> rel.type().equals(RelationshipType.CHILD))
                            .flatMap(rel -> rel.ids().stream())
                            .map(blockMap::get)
                            .filter(cellBlock -> cellBlock.blockType().equals(BlockType.CELL))
                            .forEach(cellBlock -> {
                                int rowIndex = cellBlock.rowIndex() - 1;
                                int colIndex = cellBlock.columnIndex() - 1;

                                while (tableContent.size() <= rowIndex) {
                                    tableContent.add(new ArrayList<>());
                                }
                                List<String> row = tableContent.get(rowIndex);
                                while (row.size() <= colIndex) {
                                    row.add("");
                                }
                                if(Objects.nonNull(cellBlock.relationships())) {
                                    String cellText = cellBlock.relationships().stream()
                                            .filter(rel -> rel.type().equals(RelationshipType.CHILD))
                                            .flatMap(rel -> rel.ids().stream())
                                            .map(blockMap::get)
                                            .filter(wordBlock -> wordBlock.blockType().equals(BlockType.WORD))
                                            .map(Block::text)
                                            .reduce((a, b) -> a + " " + b)
                                            .orElse("");

                                    row.set(colIndex, cellText);
                                }
                            });

                    tableData.put("content", tableContent);
                    tableData.put("pageNumber", tableBlock.page());
                    tables.add(tableData);
                });

        return tables;
    }

    private byte[] readDocument(String filePath) throws IOException {
        File file = new File(filePath);
        return Files.readAllBytes(file.toPath());
    }


}
