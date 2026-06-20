package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    @Autowired
    private GeminiService geminiService;

    // Naya endpoint: /pipeline
    @PostMapping("/pipeline")
    public String fullProductionPipeline(@RequestBody String scriptText) {
        System.out.println("--- Starting Multi-Agent Production Pipeline ---");
        
        // Step A: Agent 1 - Script ka breakdown karega
        System.out.println("[Agent 1]: Extracting Scene Details...");
        String breakdown = geminiService.getSceneBreakdown(scriptText);
        
        // Step B: Agent 2 - Breakdown dekh kar resources plan karega
        System.out.println("[Agent 2]: Planning Resources and Requirements...");
        String resources = geminiService.getResourcePlanning(breakdown);
        
        // Step C: Dono ke kaam ko ek master JSON mein jodd kar bhejna
        return "{\n" +
               "  \"sceneBreakdown\": " + breakdown + ",\n" +
               "  \"logisticsAndPlanning\": " + resources + "\n" +
               "}";
    }
}