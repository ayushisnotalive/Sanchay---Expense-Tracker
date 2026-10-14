package com.camuel.sanchay.backend.service;

import com.camuel.sanchay.backend.dto.AnalyticsDto;
import com.camuel.sanchay.backend.entity.Expense;
import com.camuel.sanchay.backend.entity.User;
import com.camuel.sanchay.backend.exception.ResourceNotFoundException;
import com.camuel.sanchay.backend.repository.ExpenseRepository;
import com.camuel.sanchay.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class AiAdvisorService {

    private static final Logger logger = LoggerFactory.getLogger(AiAdvisorService.class);

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final AnalyticsService analyticsService;
    private final RestTemplate restTemplate;

    @Value("${sanchay.ai.gemini-api-key:}")
    private String geminiApiKey;

    @Value("${sanchay.ai.gemini-model:gemini-2.0-flash}")
    private String geminiModel;

    public AiAdvisorService(UserRepository userRepository,
                            ExpenseRepository expenseRepository,
                            AnalyticsService analyticsService,
                            RestTemplateBuilder restTemplateBuilder) {
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.analyticsService = analyticsService;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(15))
                .build();
    }

    public String getFinancialAdvice(Long userId, String userPrompt) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        AnalyticsDto analytics = analyticsService.getAnalyticsSummary(userId);
        List<Expense> recentExpenses = expenseRepository.findByUserAndDeletedFalseOrderByDateDesc(user);

        // Build enterprise context
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("User Financial Overview:\n");
        contextBuilder.append("- Monthly Budget: $").append(analytics.getMonthlyBudget()).append("\n");
        contextBuilder.append("- Total Spent: $").append(analytics.getTotalExpenses()).append("\n");
        contextBuilder.append("- Remaining Budget: $").append(analytics.getRemainingBudget()).append("\n");
        contextBuilder.append("- Top Category: ").append(analytics.getTopSpendingCategory()).append("\n");
        contextBuilder.append("\nRecent Transactions:\n");

        int count = 0;
        for (Expense e : recentExpenses) {
            if (count++ >= 8) break;
            contextBuilder.append("- ").append(e.getDate()).append(": $").append(e.getAmount())
                    .append(" on ").append(e.getCategory());
            if (e.getNote() != null && !e.getNote().isBlank()) {
                contextBuilder.append(" (").append(e.getNote()).append(")");
            }
            contextBuilder.append("\n");
        }

        contextBuilder.append("\nUser Question: ").append(userPrompt);

        // Check if Gemini API key is configured
        if (geminiApiKey != null && !geminiApiKey.isBlank() && !geminiApiKey.equals("YOUR_API_KEY")) {
            try {
                String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/" 
                        + geminiModel + ":generateContent?key=" + geminiApiKey;

                String systemInstruction = "You are Sanchay's elite enterprise financial advisor. " +
                        "Provide sharp, encouraging, and data-backed financial advice based on the user's spending numbers. " +
                        "Highlight overspending risks and provide a clear 1-sentence action step.";

                Map<String, Object> requestBody = Map.of(
                        "contents", List.of(
                                Map.of("parts", List.of(
                                        Map.of("text", systemInstruction + "\n\n" + contextBuilder.toString())
                                ))
                        )
                );

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map body = response.getBody();
                    List candidates = (List) body.get("candidates");
                    if (candidates != null && !candidates.isEmpty()) {
                        Map first = (Map) candidates.get(0);
                        Map content = (Map) first.get("content");
                        if (content != null) {
                            List parts = (List) content.get("parts");
                            if (parts != null && !parts.isEmpty()) {
                                Map part = (Map) parts.get(0);
                                return (String) part.get("text");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("Gemini API call failed, using intelligent local engine: {}", e.getMessage());
            }
        }

        // Intelligent enterprise rule-based fallback
        return generateIntelligentFallbackAdvice(analytics, userPrompt);
    }

    private String generateIntelligentFallbackAdvice(AnalyticsDto analytics, String prompt) {
        double spent = analytics.getTotalExpenses() != null ? analytics.getTotalExpenses() : 0.0;
        double budget = analytics.getMonthlyBudget() != null ? analytics.getMonthlyBudget() : 1000.0;
        double ratio = budget > 0 ? (spent / budget) * 100.0 : 0.0;
        String top = analytics.getTopSpendingCategory();

        StringBuilder sb = new StringBuilder();
        if (ratio > 90.0) {
            sb.append("⚠️ **Critical Budget Alert:** You have consumed ")
              .append(String.format("%.1f", ratio))
              .append("% of your monthly budget ($").append(String.format("%.2f", spent))
              .append(" of $").append(String.format("%.2f", budget)).append(").\n\n");
            sb.append("Your highest expenditure is in **").append(top)
              .append("**. Consider freezing discretionary spending in this category for the remainder of the billing cycle.");
        } else if (ratio > 70.0) {
            sb.append("📊 **Budget Health:** You're on track but approaching your limit at ")
              .append(String.format("%.1f", ratio)).append("% utilized.\n\n")
              .append("Top spending driver: **").append(top)
              .append("**. Keep daily expenses under $")
              .append(String.format("%.2f", (budget - spent) / 10.0)).append(" to finish under budget.");
        } else {
            sb.append("✨ **Excellent Financial Control:** You have utilized only ")
              .append(String.format("%.1f", ratio)).append("% of your allocated monthly budget ($")
              .append(String.format("%.2f", spent)).append(").\n\n")
              .append("You have $").append(String.format("%.2f", analytics.getRemainingBudget()))
              .append(" available. This is a great opportunity to allocate surplus funds into your high-yield savings goals or investment holdings!");
        }

        return sb.toString();
    }
}
