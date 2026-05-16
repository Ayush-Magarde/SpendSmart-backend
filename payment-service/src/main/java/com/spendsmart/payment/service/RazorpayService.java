package com.spendsmart.payment.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class RazorpayService {

    private final RazorpayClient razorpayClient;
    private final String razorpaySecret;

    public RazorpayService(@Value("${razorpay.key}") String key,
                          @Value("${razorpay.secret}") String secret) {
        try {
            this.razorpayClient = new RazorpayClient(key, secret);
            this.razorpaySecret = secret;
            log.info("Razorpay client initialized successfully");
        } catch (RazorpayException e) {
            log.error("Failed to initialize Razorpay client: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize Razorpay client", e);
        }
    }

    public Map<String, Object> createOrder() {
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", 2000); // ₹20 in paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "order_" + System.currentTimeMillis());

            Order order = razorpayClient.orders.create(orderRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.get("id"));
            response.put("amount", order.get("amount"));
            response.put("currency", order.get("currency"));

            String orderId = order.get("id").toString();
            log.info("Razorpay order created: {}", orderId);
            return response;

        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order: {}", e.getMessage());
            throw new RuntimeException("Failed to create Razorpay order", e);
        }
    }

    public boolean verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        try {
            String data = razorpayOrderId + "|" + razorpayPaymentId;
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(razorpaySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] signatureBytes = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : signatureBytes) {
                sb.append(String.format("%02x", b));
            }
            String generatedSignature = sb.toString();

            boolean isValid = generatedSignature.equals(razorpaySignature);

            if (isValid) {
                log.info("Payment verification successful for order: {}", razorpayOrderId);
            } else {
                log.warn("Payment verification failed for order: {}", razorpayOrderId);
            }

            return isValid;

        } catch (Exception e) {
            log.error("Error verifying payment: {}", e.getMessage());
            return false;
        }
    }
}
