package com.kkrishnan.docanalyzer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.kkrishnan.docanalyzer.models.ChatCompletionRequest;
import com.kkrishnan.docanalyzer.models.ChatCompletionRequestMessage;
import com.kkrishnan.docanalyzer.models.ChatCompletionResponse;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

@Service
public class LLMService {


    private String API_KEY;
    public LLMService(@Value("${openai.key}") String openAIApiKey){
        this.API_KEY = openAIApiKey;
    }

    private static final String MODEL = "gpt-4o-mini";

    public String extractDates(String text) {
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();

        HashMap<String, String> messages = new HashMap<String, String>(){{

            put("system", "You are a helpful assistant that extracts important dates from text.");
            put("user", "Extract all important dates from the following text, listing them in YYYY-MM-DD format if possible. Return the result as a json with key value pairs where the key is the text associated with the date and the value is the date itself. Text: " + text);

        }};

        ChatCompletionRequest request = new ChatCompletionRequest(MODEL, messages.entrySet().stream().map(e -> new ChatCompletionRequestMessage(e.getKey(), e.getValue())).toList());
        Type listOfMessagesObject = new TypeToken<ChatCompletionRequest>(){}.getType();
        String jsonRequest = gson.toJson(request, listOfMessagesObject);
        RequestBody requestBody = RequestBody.create(jsonRequest, MediaType.parse("application/json"));

        Request req = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(req).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                ChatCompletionResponse chatCompletionResponse = gson.fromJson(responseBody, ChatCompletionResponse.class);
                return chatCompletionResponse.getChoices().getFirst().getMessage().getContent();
            } else {
                System.out.println("OpenAI API request failed with response: " + response.body().string());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
