package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Service // Yeh Spring Boot ko batata hai ki yeh class ek Bean hai jise wo manage karega
public class GeminiService {

    // Yeh aapki application.properties se API key automatically utha lega
    @Value("${gemini.api.key}")
    private String apiKey;

    public String getSceneBreakdown(String scriptText) {
        try {
            // THE FIX: Updated to the live 'gemini-2.5-flash' model 
            String urlString = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;
            java.net.URI uri = new java.net.URI(urlString);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String prompt = "You are a professional film Line Producer. Read this scene and extract the details. Return ONLY a valid JSON object with these exact keys: 'status' (Success or Failed), 'location' (the place), 'time' (Day or Night), and 'characters' (list of people). Do not write any markdown, just the JSON. Scene text: " + scriptText;

            String cleanScript = prompt.replace("\"", "\\\"").replace("\n", " ");
            String requestBody = "{\"contents\": [{\"parts\":[{\"text\": \"" + cleanScript + "\"}]}]}";

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

           ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
            
            // NAYA CODE: Desi Hack (Bina kisi import ke JSON nikalna)
            String rawResponse = response.getBody();
            
            // AI ke response mein se "text": wali line dhoondhna
            int startIndex = rawResponse.indexOf("\"text\": \"");
            
            if (startIndex != -1) {
                // Asli JSON '{' se shuru hota hai
                int jsonStart = rawResponse.indexOf("{", startIndex);
                // Aur '}' par khatam hota hai
                int jsonEnd = rawResponse.indexOf("}", jsonStart);
                
                // Sirf kaam ka hissa kaat kar nikalna
                String cleanResult = rawResponse.substring(jsonStart, jsonEnd + 1);
                
                // AI ke extra slashes (\") ko normal quotes (") mein badalna
                return cleanResult.replace("\\\"", "\"").replace("\\n", "");
            }
            
            return rawResponse;
            
            
        } catch (Exception e) {
            System.out.println("GEMINI API ERROR: " + e.getMessage()); 
            e.printStackTrace();
            return "{\"error\": \"AI engine se connect nahi ho paya. Error: " + e.getMessage() + "\"}";
        }
    }
    public String getResourcePlanning(String breakdownJson) {
        try {
            // Humara latest live model
            String urlString = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;
            java.net.URI uri = new java.net.URI(urlString);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Agent 2 ka Prompt: Yeh breakdown dekh kar planning karega
            String prompt = "You are an expert Film Production Manager. Analyze this scene breakdown JSON and provide a logistical requirement list. Return ONLY a valid JSON object with these exact keys: 'requiredPermissions' (list of permissions needed for the location), 'equipment' (cameras, lights needed for that time), and 'estimatedCrewSize' (number). Do not write markdown, just the JSON. Breakdown Data: " + breakdownJson;

            String cleanPrompt = prompt.replace("\"", "\\\"").replace("\n", " ");
            String requestBody = "{\"contents\": [{\"parts\":[{\"text\": \"" + cleanPrompt + "\"}]}]}";

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
            
            // Wahi Desi hack jisse extra kachra hat jaye aur clean JSON mile
            String rawResponse = response.getBody();
            int startIndex = rawResponse.indexOf("\"text\": \"");
            if (startIndex != -1) {
                int jsonStart = rawResponse.indexOf("{", startIndex);
                int jsonEnd = rawResponse.indexOf("}", jsonStart);
                String cleanResult = rawResponse.substring(jsonStart, jsonEnd + 1);
                return cleanResult.replace("\\\"", "\"").replace("\\n", "");
            }
            return rawResponse;
            
        } catch (Exception e) {
            return "{\"error\": \"Resource Agent fail ho gaya: " + e.getMessage() + "\"}";
        }
    }
}