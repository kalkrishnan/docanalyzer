package com.kkrishnan.docanalyzer.models;

import lombok.Data;

@Data
public class ChatCompletionResponseChoices {
    Integer index;
    ChatCompletionResponseChoiceMessage message;
}
