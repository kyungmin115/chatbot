package com.chatbot.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatResponse {

    private String response;
    private String emotion;
    private boolean crisisDetected;

    public ChatResponse() {}

    public ChatResponse(String response, String emotion, boolean crisisDetected) {
        this.response = response;
        this.emotion = emotion;
        this.crisisDetected = crisisDetected;
    }
}
