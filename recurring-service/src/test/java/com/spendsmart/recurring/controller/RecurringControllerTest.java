package com.spendsmart.recurring.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsmart.recurring.dto.RecurringRuleRequest;
import com.spendsmart.recurring.entity.RecurringRule;
import com.spendsmart.recurring.service.RecurringRuleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecurringController.class)
@org.springframework.context.annotation.Import(com.spendsmart.recurring.config.SecurityConfig.class)
class RecurringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecurringRuleService recurringRuleService;

    @Test
    void createRecurringRule() throws Exception {
        RecurringRuleRequest request = new RecurringRuleRequest();
        request.setName("Netflix");
        request.setType("EXPENSE");
        request.setAmount(10.0);
        request.setFrequency("MONTHLY");

        RecurringRule rule = new RecurringRule();
        rule.setId(1L);

        when(recurringRuleService.addRecurringRule(eq(1L), eq("test@test.com"), any())).thenReturn(rule);

        mockMvc.perform(post("/api/recurring")
                        .header("X-User-Email", "test@test.com")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getRecurringRules() throws Exception {
        RecurringRule rule = new RecurringRule();
        rule.setId(1L);

        when(recurringRuleService.getRecurringRules(1L)).thenReturn(List.of(rule));

        mockMvc.perform(get("/api/recurring")
                        .header("X-User-Email", "test@test.com")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void updateRecurringRule() throws Exception {
        RecurringRuleRequest request = new RecurringRuleRequest();
        request.setName("Netflix");
        request.setType("EXPENSE");
        request.setAmount(10.0);
        request.setFrequency("MONTHLY");

        RecurringRule rule = new RecurringRule();
        rule.setId(1L);

        when(recurringRuleService.updateRecurringRule(eq(1L), eq(1L), any())).thenReturn(rule);

        mockMvc.perform(put("/api/recurring/1")
                        .header("X-User-Email", "test@test.com")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteRecurringRule() throws Exception {
        doNothing().when(recurringRuleService).deleteRecurringRule(1L, 1L);

        mockMvc.perform(delete("/api/recurring/1")
                        .header("X-User-Email", "test@test.com")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk());
    }
}
