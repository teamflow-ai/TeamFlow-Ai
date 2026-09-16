package com.teamflow.ai.ai.controller;

import com.teamflow.ai.ai.dto.request.PolicyChatRequest;
import com.teamflow.ai.ai.dto.response.PolicyChatResponse;
import com.teamflow.ai.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/ai/policy")
@RequiredArgsConstructor
public class PolicyAssistantController {

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<PolicyChatResponse>> chat(@Valid @RequestBody PolicyChatRequest request) {
        log.info("Received policy chat query: {}", request.getQuery());
        
        // Mock response for now to demonstrate RAG integration progress
        PolicyChatResponse mockResponse = PolicyChatResponse.builder()
                .answer("I am analyzing the company HR handbook and benefits policies for you regarding: \"" + request.getQuery() + "\".\n\n[RAG vector search integration is currently in progress...]")
                .source("Pending RAG Knowledge Base")
                .build();
                
        return ResponseEntity.ok(ApiResponse.success(mockResponse));
    }
}
