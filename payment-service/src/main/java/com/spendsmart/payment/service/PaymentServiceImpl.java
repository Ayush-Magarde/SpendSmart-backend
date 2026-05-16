package com.spendsmart.payment.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Payment service now focuses exclusively on Razorpay premium payments
// No payment method CRUD operations are needed - PaymentService interface is empty
// All payment operations are now handled through RazorpayService

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    // Implementation is intentionally empty as payment method CRUD has been removed
    // All payment operations are now handled through RazorpayService
}
