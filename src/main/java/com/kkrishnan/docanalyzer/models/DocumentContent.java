package com.kkrishnan.docanalyzer.models;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public record DocumentContent(String extractedText,  List<Map<String, Object>> extractedTables) implements Serializable {
}
