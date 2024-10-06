package com.kkrishnan.docanalyzer.models;

import lombok.Data;

import java.util.List;

@Data
public class ChatCompletionResponse {
    String id;
    List<ChatCompletionResponseChoices> choices;
    Integer created;
}
