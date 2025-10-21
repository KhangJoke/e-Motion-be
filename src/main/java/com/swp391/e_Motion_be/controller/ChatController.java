package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.chat.ChatRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.service.ChatService;
import jakarta.servlet.http.HttpSession;
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
    public ApiResponse<String> chatMessage(@RequestBody ChatRequest request, HttpSession session){
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Chat response generated successfully");
        response.setData(chatService.generation(request, session.getId()));
        return response;
    }
}
