package com.chatbot.service;


import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmotionService {

    private static final String[] CRISIS_KEYWORDS = {
            "자살", "죽고싶", "살기싫", "죽어버리", "자해"
    };

    public String extractEmotion(String response) {
        Pattern pattern = Pattern.compile("\\[감정:\\s*([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    public boolean detectCrisis(String message) {
        String lowerMessage = message.toLowerCase();
        for (String keyword : CRISIS_KEYWORDS) {
            if(lowerMessage.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
