package com.lms.service;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ChatService {

    private final Optional<ChatClient> chatClient;
    private Map<String, String> userSessions = new HashMap<>();

    public ChatService(@Autowired(required = false) ChatClient.Builder chatClientBuilder) {
        if (chatClientBuilder != null) {
            this.chatClient = Optional.of(chatClientBuilder.build());
        } else {
            this.chatClient = Optional.empty();
        }
    }

    public String getResponse(String sessionId, String message) {
        String input = message.toLowerCase();
        
        // Name Recognition (Still useful for context)
        if (input.contains("i am") || input.contains("my name is")) {
            String name = extractName(message);
            userSessions.put(sessionId, name);
        }

        // AI Dynamic Response (Handle everything)
        if (chatClient.isPresent()) {
            try {
                String name = userSessions.get(sessionId);
                String aiPrompt = String.format(
                    "You are the 'AppTechno Careers' AI Support assistant. " +
                    "Identity: You represent AppTechno Careers (an IT/Non-IT training & placement institute). " +
                    "Knowledge: We offer Java Full Stack, Python AI, MERN, and Software Testing. We have 500+ partners and a 'Pay 50%% After Placement' model. " +
                    "Tone: Professional, helpful, and concise. " +
                    "Context: The user's name is %s. " +
                    "User Message: %s. " +
                    "Respond to the user naturally. If they ask about courses, explain them. If they ask anything else, be helpful but stay professional.",
                    (name != null ? name : "friend"), message
                );
                
                String response = chatClient.get().prompt(aiPrompt).call().content();
                return (response != null && !response.isEmpty()) ? response : "I'm processing that. How else can I assist with your career goals?";
            } catch (Exception e) {
                System.err.println("DEBUG: AI Chat Error: " + e.getMessage());
            }
        }

        // Reliable Fallback if AI or Client fails
        return "Thanks for reaching out! AppTechno Careers offers top-tier training in Java, Python, and MERN with placement support. How can I guide your career path today?";
    }

    private String extractName(String message) {
        String[] words = message.split("\\s+");
        // Simple logic: take the last word if it's not "am" or "is"
        for (int i = words.length - 1; i >= 0; i--) {
            String w = words[i].toLowerCase();
            if (!w.equals("am") && !w.equals("is") && !w.equals("i") && !w.equals("my") && !w.equals("name")) {
                return words[i];
            }
        }
        return "friend";
    }
}
