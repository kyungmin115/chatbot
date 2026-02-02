package com.chatbot.model;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChatRequest {

    private String message;
    private List<Message> history;

    public ChatRequest() {}

}
