package com.tripweaver.controller;

import com.tripweaver.dto.ChatMessageDto;
import com.tripweaver.dto.SendMessageRequest;
import com.tripweaver.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        String response = chatService.sendMessage(request.getPlanId(), request.getMessage());
        return ResponseEntity.ok(Map.of("response", response));
    }

    @PostMapping("/new")
    public ResponseEntity<Map<String, Long>> createNewPlan() {
        Long planId = chatService.createNewPlan();
        return ResponseEntity.ok(Map.of("planId", planId));
    }

    @GetMapping("/history/{planId}")
    public ResponseEntity<List<ChatMessageDto>> getHistory(@PathVariable Long planId) {
        return ResponseEntity.ok(chatService.getHistory(planId));
    }
}