package com.kkrishnan.docanalyzer;

import com.kkrishnan.docanalyzer.models.DocumentContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.textract.model.Block;
import software.amazon.awssdk.services.textract.model.BlockType;
import software.amazon.awssdk.services.textract.model.GetDocumentAnalysisResponse;
import software.amazon.awssdk.services.textract.model.RelationshipType;

import java.util.*;

@Service
public class DocAnalyzerService {

    @Autowired
    private AmazonTextractService textractService;

    @Cacheable(value = "textractCache",  key = "#jobId")
    public DocumentContent getAnalysisResults(String jobId){
        StringBuffer extractedText = new StringBuffer();
        String nextToken = null;
        List<Map<String, Object>> extractedTables = new ArrayList<>();

        do {


            GetDocumentAnalysisResponse result = textractService.getDocumentAnalysisResults(jobId, nextToken);

            for (Block block : result.blocks()) {
                if (block.blockType().equals(BlockType.LINE)) {
                    extractedText.append(block.text());
                    extractedText.append(System.lineSeparator());
                }
            }
            extractedTables.addAll(processTables(result.blocks()));

            nextToken = result.nextToken();
        } while (nextToken != null);

        return new DocumentContent(extractedText.toString(),extractedTables);
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
                                if (Objects.nonNull(cellBlock.relationships())) {
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
}