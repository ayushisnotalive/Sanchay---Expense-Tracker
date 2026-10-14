package com.camuel.sanchay.backend;

import com.camuel.sanchay.backend.dto.AuthDto.LoginRequest;
import com.camuel.sanchay.backend.dto.BackupPayloadDto;
import com.camuel.sanchay.backend.dto.ExpenseDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BackupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testExportAndImportBackup() throws Exception {
        // Login with seeded demo user
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsernameOrEmail("user@sanchay.com");
        loginReq.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginJson.get("data").get("accessToken").asText();

        // 1. Export backup
        mockMvc.perform(get("/api/v1/backup/export")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.categories").isArray())
                .andExpect(jsonPath("$.data.wallets").isArray());

        // 2. Import backup with a sample expense
        BackupPayloadDto payload = new BackupPayloadDto();
        ExpenseDto expense = new ExpenseDto();
        expense.setAmount("75.50");
        expense.setCategory("Food");
        expense.setDate("2026-10-06");
        expense.setNote("Backup import test dinner");
        expense.setPaymentMethod("Card");
        payload.setExpenses(List.of(expense));

        mockMvc.perform(post("/api/v1/backup/import")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.expenses").value(1));
    }
}
