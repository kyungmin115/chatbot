package com.chatbot.service;

import com.chatbot.model.Message;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash-latest:generateContent";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();


    public String sendMessage(List<Message> history, String userMessage) throws IOException {
        String systemPrompt = "당신은 공감 능력이 뛰어난 심리 상담 챗봇입니다. " +
                "사용자의 감정을 세심하게 분석하고, 따뜻하고 지지적인 태도로 응답하세요. " +
                "각 응답의 시작 부분에 감지된 감정을 [감정: 슬픔], [감정: 불안] 등의 형식으로 표시하세요. " +
                "전문적인 치료가 필요해 보이는 경우 전문가 상담을 권유하세요.\n\n";

        StringBuilder conversationContext = new StringBuilder(systemPrompt);

        for(Message msg : history) {
            if (msg.getRole().equals("user")) {
                conversationContext.append("사용자 : ").append(msg.getContent()).append("\n");
            } else {
                conversationContext.append("상담봇 :").append(msg.getContent()).append("\n");
            }
        }

        conversationContext.append("사용자 : ").append(userMessage).append("\n상담봇 : ");

        //Api 요청 생성
        JsonObject requestBody = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();

        part.addProperty("text", conversationContext.toString());
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);

        requestBody.add("contents", contents);

        // 필터 설정
        JsonArray safetySettings = new JsonArray();
        String[] categories = {
                "HARM_CATEGORY_HARASSMENT",
                "HARM_CATEGORY_HATE_SPEECH",
                "HARM_CATEGORY_SEXUALLY_EXPLICIT",
                "HARM_CATEGORY_DANGEROUS_CONTENT"
        };

        for(String category : categories) {
            JsonObject setting = new JsonObject();
            setting.addProperty("category", category);
            setting.addProperty("threshold", "BLOCK_NONE");
            safetySettings.add(setting);
        }
        requestBody.add("safetySettings", safetySettings);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.7);
        generationConfig.addProperty("topK", 40);
        generationConfig.addProperty("topP", 0.95);
        generationConfig.addProperty("maxOutputTokens", 1024);
        requestBody.add("generationConfig", generationConfig);

        RequestBody body = RequestBody.create(
                requestBody.toString(), MediaType.parse("application/json")
        );

        String urlWithKey = API_URL + "?key=" + apiKey;

        Request request = new Request.Builder()
                .url(urlWithKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try(Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error Body";
                throw new IOException("API 요청 실패 : " + response.code() + " - " + errorBody);
            }

            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            // 제미나이 응답
            String  assistantMessage = jsonResponse
                    .getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();

            //히스토리 추가
            history.add(new Message("user", userMessage));
            history.add(new Message("assistant", assistantMessage));

            return assistantMessage;
        }


    }
}
