package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.chat.ChatRequest;
import com.swp391.e_Motion_be.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/message")
    public String chatMessage(@RequestBody ChatRequest request){
        return chatService.generation(request);
    }
}
