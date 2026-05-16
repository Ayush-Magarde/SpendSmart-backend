package com.spendsmart.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsmart.notification.dto.NotificationRequest;
import com.spendsmart.notification.entity.Notification;
import com.spendsmart.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@org.springframework.context.annotation.Import(com.spendsmart.notification.config.SecurityConfig.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @Test
    void createNotification_ReturnsNotification() throws Exception {
        NotificationRequest request = new NotificationRequest();
        request.setTitle("Test");
        request.setMessage("Message");
        request.setType("INFO");
        request.setCategory("SYSTEM");

        Notification notification = new Notification();
        notification.setId(1L);
        notification.setTitle("Test");

        when(notificationService.addNotification(eq(1L), eq("test@test.com"), any(NotificationRequest.class)))
                .thenReturn(notification);

        mockMvc.perform(post("/api/notifications")
                        .header("X-User-Email", "test@test.com")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getNotifications_ReturnsList() throws Exception {
        Notification notification = new Notification();
        notification.setId(1L);

        when(notificationService.getNotifications(1L)).thenReturn(List.of(notification));

        mockMvc.perform(get("/api/notifications")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void markAsRead_ReturnsOk() throws Exception {
        doNothing().when(notificationService).markAsRead(1L, 1L);

        mockMvc.perform(patch("/api/notifications/1/read")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk());
    }
}
