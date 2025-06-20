package com.api.musiconnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.musiconnect.service.ai.GeminiChatService;

import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para el chatbot de MusiConnect.
 * Requiere autenticación JWT (filtrada por SecurityConfig).
 */
@RestController
@RequestMapping("/ai-chat")
@RequiredArgsConstructor
public class ChatController {

    private final GeminiChatService geminiService;

    /**
     * Endpoint que envía la pregunta a Gemini y devuelve la respuesta.
     * @param question Texto en JSON plano ("text/plain"), o puede ser campo JSON.
     * @return Respuesta generada por IA.
     */
    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody String question) {
        String answer = geminiService.chat(question);
        return ResponseEntity.ok(answer);
    }
} 