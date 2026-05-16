package com.spendsmart.summary.controller;

import com.spendsmart.summary.dto.SummaryResponse;
import com.spendsmart.summary.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService summaryService;

    @GetMapping
    public SummaryResponse getSummary(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") Long userId) {

        return summaryService.getSummary(email, userId);
    }
}