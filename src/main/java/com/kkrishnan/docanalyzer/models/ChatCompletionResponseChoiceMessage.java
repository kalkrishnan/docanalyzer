package com.kkrishnan.docanalyzer.models;

import lombok.Data;

@Data
public class ChatCompletionResponseChoiceMessage {
    String content;
    String role;
}
