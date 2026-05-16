package com.spendsmart.payment.controller;

import com.spendsmart.payment.dto.PaymentVerifyRequest;
import com.spendsmart.payment.dto.PaymentVerifyResponse;
import com.spendsmart.payment.entity.PremiumPayment;
import com.spendsmart.payment.repository.PremiumPaymentRepository;
import com.spendsmart.payment.service.RazorpayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Management", description = "APIs for managing premium payments")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final RazorpayService razorpayService;
    private final PremiumPaymentRepository premiumPaymentRepository;
    private final com.spendsmart.payment.client.AuthClient authClient;

    @PostMapping("/create-order")
    @Operation(summary = "Create Razorpay order for premium payment")
    public Map<String, Object> createOrder(
            @RequestHeader("X-User-Email") String email) {

        log.info("Creating Razorpay order for user: {}", email);
        return razorpayService.createOrder();
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify Razorpay payment and mark user as premium")
    public PaymentVerifyResponse verifyPayment(
            @RequestHeader("X-User-Email") String email,
            @Valid @RequestBody PaymentVerifyRequest request) {

        log.info("Verifying payment for user: {}, order: {}", email, request.getRazorpayOrderId());

        boolean isValid = razorpayService.verifyPayment(
            request.getRazorpayOrderId(),
            request.getRazorpayPaymentId(),
            request.getRazorpaySignature()
        );

        if (isValid) {
            try {
                // Update local payment record
                PremiumPayment premiumPayment = premiumPaymentRepository.findByUserEmail(email)
                    .orElse(new PremiumPayment());

                premiumPayment.setUserEmail(email);
                premiumPayment.setIsPremium(true);
                premiumPayment.setRazorpayOrderId(request.getRazorpayOrderId());
                premiumPayment.setRazorpayPaymentId(request.getRazorpayPaymentId());
                premiumPayment.setUpdatedAt(LocalDateTime.now());

                if (premiumPayment.getId() == null) {
                    premiumPayment.setCreatedAt(LocalDateTime.now());
                }

                premiumPaymentRepository.save(premiumPayment);
                
                // Update user status in Auth Service
                authClient.upgradeUser(email);
                
                log.info("User {} marked as premium in both services", email);
                return new PaymentVerifyResponse(true, "Payment verified successfully. User is now premium.");
            } catch (Exception e) {
                log.error("Error marking user as premium: {}", e.getMessage());
                return new PaymentVerifyResponse(false, "Payment verified but failed to update premium status.");
            }
        } else {
            log.warn("Payment verification failed for user: {}", email);
            return new PaymentVerifyResponse(false, "Payment verification failed.");
        }
    }

    @GetMapping("/premium-status")
    @Operation(summary = "Check if user has premium status")
    public Map<String, Boolean> getPremiumStatus(
            @RequestHeader("X-User-Email") String email) {

        log.info("Checking premium status for user: {}", email);
        boolean isPremium = premiumPaymentRepository.existsByUserEmail(email);
        return Map.of("isPremium", isPremium);
    }
}
