package com.spendsmart.summary.controller;

import com.spendsmart.summary.dto.SummaryResponse;
import com.spendsmart.summary.service.SummaryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SummaryController.class)
@org.springframework.context.annotation.Import(com.spendsmart.summary.config.SecurityConfig.class)
class SummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SummaryService summaryService;

    @Test
    void getSummary_ReturnsSummaryResponse() throws Exception {
        String email = "test@example.com";
        Long userId = 1L;

        SummaryResponse mockResponse = new SummaryResponse(
                5000.0,
                2000.0,
                3000.0,
                Map.of("Food", 2000.0),
                List.of()
        );

        when(summaryService.getSummary(email, userId)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/summary")
                        .header("X-User-Email", email)
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(5000.0))
                .andExpect(jsonPath("$.totalExpense").value(2000.0))
                .andExpect(jsonPath("$.balance").value(3000.0))
                .andExpect(jsonPath("$.categoryBreakdown.Food").value(2000.0));
    }

    @Test
    void getSummary_MissingHeaders_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/summary"))
                .andExpect(status().isBadRequest());
    }
}
