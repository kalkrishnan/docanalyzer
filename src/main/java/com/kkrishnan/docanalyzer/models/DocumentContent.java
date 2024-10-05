package com.kkrishnan.docanalyzer.models;

import java.util.List;
import java.util.Map;

public record DocumentContent(String title, String source, List<String> extractedText,  List<Map<String, Object>> extractedTables) {
}
