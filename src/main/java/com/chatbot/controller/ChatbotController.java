package com.chatbot.controller;


import com.chatbot.model.ChatRequest;
import com.chatbot.model.ChatResponse;
import com.chatbot.model.Message;
import com.chatbot.service.EmotionService;
import com.chatbot.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatbotController {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private EmotionService emotionService;

    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        try {
            List<Message> history = request.getHistory();
            if (history == null) {
                history = new ArrayList<>();
            }
            // 위험
            boolean crisis = emotionService.detectCrisis(request.getMessage());

            //제미나이 사용
            String response = geminiService.sendMessage(history, request.getMessage());

            String emotion = emotionService.extractEmotion(response);

            ChatResponse chatResponse = new ChatResponse(response, emotion, crisis);

            return ResponseEntity.ok(chatResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(new ChatResponse("죄송합니다. 오류가 발생했습니다 : " + e.getMessage(), null, false));
        }
    }
}
