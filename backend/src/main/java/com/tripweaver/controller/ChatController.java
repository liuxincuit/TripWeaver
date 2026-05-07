package com.tripweaver.controller;

import com.tripweaver.entity.Conversation;
import com.tripweaver.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendMessage(@RequestBody Map<String, Object> request) {
        Long planId = Long.valueOf(request.get("planId").toString());
        String message = request.get("message").toString();

        String response = chatService.sendMessage(planId, message);
        return ResponseEntity.ok(Map.of("response", response));
    }

    @PostMapping("/new")
    public ResponseEntity<Map<String, Long>> createNewPlan() {
        Long planId = chatService.createNewPlan();
        return ResponseEntity.ok(Map.of("planId", planId));
    }

    @GetMapping("/history/{planId}")
    public ResponseEntity<Conversation> getHistory(@PathVariable Long planId) {
        return chatService.getConversation(planId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}