package com.kkrishnan.docanalyzer.models;

import java.util.List;

public record ChatCompletionRequest(String model, List<ChatCompletionRequestMessage> messages) {
}


