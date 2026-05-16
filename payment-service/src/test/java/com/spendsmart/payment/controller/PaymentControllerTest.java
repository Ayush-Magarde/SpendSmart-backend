package com.spendsmart.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsmart.payment.client.AuthClient;
import com.spendsmart.payment.dto.PaymentVerifyRequest;
import com.spendsmart.payment.entity.PremiumPayment;
import com.spendsmart.payment.repository.PremiumPaymentRepository;
import com.spendsmart.payment.service.RazorpayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@Import(com.spendsmart.payment.config.SecurityConfig.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RazorpayService razorpayService;

    @MockBean
    private PremiumPaymentRepository premiumPaymentRepository;

    @MockBean
    private AuthClient authClient;

    @Autowired
    private ObjectMapper objectMapper;

    private PaymentVerifyRequest validVerifyRequest;

    @BeforeEach
    void setUp() {
        validVerifyRequest = new PaymentVerifyRequest();
        validVerifyRequest.setRazorpayOrderId("order_abc123");
        validVerifyRequest.setRazorpayPaymentId("pay_xyz789");
        validVerifyRequest.setRazorpaySignature("valid_sig");
    }

    // ── POST /api/payments/create-order ─────────────────────────────────────

    @Test
    void createOrder_returns200WithOrderDetails() throws Exception {
        Map<String, Object> orderResponse = Map.of(
                "orderId", "order_abc123",
                "amount", 2000,
                "currency", "INR"
        );
        when(razorpayService.createOrder()).thenReturn(orderResponse);

        mockMvc.perform(post("/api/payments/create-order")
                        .header("X-User-Email", "user@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("order_abc123"))
                .andExpect(jsonPath("$.amount").value(2000));

        verify(razorpayService).createOrder();
    }

    // ── POST /api/payments/verify ────────────────────────────────────────────

    @Test
    void verifyPayment_returnsSuccessWhenValidAndNewUser() throws Exception {
        when(razorpayService.verifyPayment(anyString(), anyString(), anyString())).thenReturn(true);
        when(premiumPaymentRepository.findByUserEmail("user@test.com")).thenReturn(Optional.empty());
        when(premiumPaymentRepository.save(any(PremiumPayment.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(authClient).upgradeUser("user@test.com");

        mockMvc.perform(post("/api/payments/verify")
                        .header("X-User-Email", "user@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validVerifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Payment verified successfully. User is now premium."));

        verify(premiumPaymentRepository).save(any(PremiumPayment.class));
        verify(authClient).upgradeUser("user@test.com");
    }

    @Test
    void verifyPayment_returnsSuccessWhenValidAndExistingUser() throws Exception {
        PremiumPayment existing = new PremiumPayment();
        existing.setUserEmail("user@test.com");

        when(razorpayService.verifyPayment(anyString(), anyString(), anyString())).thenReturn(true);
        when(premiumPaymentRepository.findByUserEmail("user@test.com")).thenReturn(Optional.of(existing));
        when(premiumPaymentRepository.save(any(PremiumPayment.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(authClient).upgradeUser("user@test.com");

        mockMvc.perform(post("/api/payments/verify")
                        .header("X-User-Email", "user@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validVerifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void verifyPayment_returnsFalseWhenSignatureInvalid() throws Exception {
        when(razorpayService.verifyPayment(anyString(), anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/api/payments/verify")
                        .header("X-User-Email", "user@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validVerifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Payment verification failed."));

        verify(premiumPaymentRepository, never()).save(any());
    }

    @Test
    void verifyPayment_returnsFalseWhenAuthClientFails() throws Exception {
        when(razorpayService.verifyPayment(anyString(), anyString(), anyString())).thenReturn(true);
        when(premiumPaymentRepository.findByUserEmail("user@test.com")).thenReturn(Optional.empty());
        when(premiumPaymentRepository.save(any(PremiumPayment.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("Auth service down")).when(authClient).upgradeUser("user@test.com");

        mockMvc.perform(post("/api/payments/verify")
                        .header("X-User-Email", "user@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validVerifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Payment verified but failed to update premium status."));
    }

    // ── GET /api/payments/premium-status ────────────────────────────────────

    @Test
    void getPremiumStatus_returnsTrueWhenPremium() throws Exception {
        when(premiumPaymentRepository.existsByUserEmail("user@test.com")).thenReturn(true);

        mockMvc.perform(get("/api/payments/premium-status")
                        .header("X-User-Email", "user@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isPremium").value(true));
    }

    @Test
    void getPremiumStatus_returnsFalseWhenNotPremium() throws Exception {
        when(premiumPaymentRepository.existsByUserEmail("user@test.com")).thenReturn(false);

        mockMvc.perform(get("/api/payments/premium-status")
                        .header("X-User-Email", "user@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isPremium").value(false));
    }
}
