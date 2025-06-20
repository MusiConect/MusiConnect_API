package com.api.musiconnect.service.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

import lombok.NonNull;

/**
 * Servicio de envoltura para invocar la Gemini API mediante API-Key.
 * <p>
 * Nota: la clave se inyecta desde application.properties como variable de entorno
 * {@code GEMINI_API_KEY}. Nunca debe ser comiteada en el repositorio.
 */
@Service
public class GeminiChatService {

    /**
     * Cliente reutilizable y thread-safe para invocaciones a Gemini.
     */
    private final Client client;

    public GeminiChatService(@Value("${gemini.api-key}") @NonNull String apiKey) {
        this.client = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    /**
     * Envía un mensaje al modelo Gemini y devuelve la respuesta de texto.
     *
     * @param prompt Consulta del usuario.
     * @return Respuesta generada por el modelo.
     */
    public String chat(@NonNull String prompt) {
        GenerateContentResponse rsp = client.models
                .generateContent("gemini-2.5-flash", prompt, null);
        return rsp.text();
    }
} 