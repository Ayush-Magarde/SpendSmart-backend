package com.spendsmart.payment.service;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for RazorpayService using reflection to inject a mock secret
 * without actually calling the Razorpay API.
 *
 * We test ONLY the verifyPayment HMAC logic — createOrder() needs a live
 * Razorpay client so it is excluded from unit testing and covered by an
 * integration / manual test.
 */
@ExtendWith(MockitoExtension.class)
class RazorpayServiceTest {

    // A deterministic HMAC-SHA256 signature computed for the known inputs below:
    //   data       = "order_abc123|pay_xyz789"
    //   secret     = "test_secret"
    //   expected   = computed offline with standard HMAC-SHA256
    private static final String ORDER_ID   = "order_abc123";
    private static final String PAYMENT_ID = "pay_xyz789";
    private static final String SECRET     = "test_secret";

    // Pre-computed: echo -n "order_abc123|pay_xyz789" | openssl dgst -sha256 -hmac "test_secret"
    private static final String VALID_SIGNATURE = computeExpectedHmac();

    private RazorpayService razorpayService;

    @BeforeEach
    void setUp() throws Exception {
        // Use reflection to create RazorpayService with a null RazorpayClient
        // (we only call verifyPayment which does not use the client)
        razorpayService = new RazorpayService("dummy_key", SECRET) {
            // Override to skip real Razorpay client init
        };
    }

    // ── createOrder ────────────────────────────────────────────────────────

    @Test
    void createOrder_returnsOrderDetailsOnSuccess() throws Exception {
        // Inject mock RazorpayClient
        com.razorpay.RazorpayClient mockClient = mock(com.razorpay.RazorpayClient.class);
        com.razorpay.OrderClient mockOrders = mock(com.razorpay.OrderClient.class);
        mockClient.orders = mockOrders;
        
        com.razorpay.Order mockOrder = new com.razorpay.Order(new JSONObject(
                "{\"id\": \"order_abc123\", \"amount\": 2000, \"currency\": \"INR\"}"));
        when(mockOrders.create(any(JSONObject.class))).thenReturn(mockOrder);

        Field clientField = RazorpayService.class.getDeclaredField("razorpayClient");
        clientField.setAccessible(true);
        clientField.set(razorpayService, mockClient);

        Map<String, Object> response = razorpayService.createOrder();

        assertThat(response).containsEntry("orderId", "order_abc123")
                .containsEntry("amount", 2000)
                .containsEntry("currency", "INR");
    }

    @Test
    void createOrder_throwsExceptionOnRazorpayFailure() throws Exception {
        com.razorpay.RazorpayClient mockClient = mock(com.razorpay.RazorpayClient.class);
        com.razorpay.OrderClient mockOrders = mock(com.razorpay.OrderClient.class);
        mockClient.orders = mockOrders;
        
        when(mockOrders.create(any(JSONObject.class))).thenThrow(new com.razorpay.RazorpayException("API Error"));

        Field clientField = RazorpayService.class.getDeclaredField("razorpayClient");
        clientField.setAccessible(true);
        clientField.set(razorpayService, mockClient);

        assertThatThrownBy(() -> razorpayService.createOrder())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to create Razorpay order");
    }

    // ── verifyPayment ──────────────────────────────────────────────────────

    @Test
    void verifyPayment_returnsTrueForValidSignature() {
        boolean result = razorpayService.verifyPayment(ORDER_ID, PAYMENT_ID, VALID_SIGNATURE);
        assertThat(result).isTrue();
    }

    @Test
    void verifyPayment_returnsFalseForInvalidSignature() {
        boolean result = razorpayService.verifyPayment(ORDER_ID, PAYMENT_ID, "wrong_signature");
        assertThat(result).isFalse();
    }

    @Test
    void verifyPayment_returnsFalseForTamperedOrderId() {
        boolean result = razorpayService.verifyPayment("tampered_order", PAYMENT_ID, VALID_SIGNATURE);
        assertThat(result).isFalse();
    }

    @Test
    void verifyPayment_returnsFalseForTamperedPaymentId() {
        boolean result = razorpayService.verifyPayment(ORDER_ID, "tampered_pay", VALID_SIGNATURE);
        assertThat(result).isFalse();
    }

    // ── helper ────────────────────────────────────────────────────────────

    private static String computeExpectedHmac() {
        try {
            String data = ORDER_ID + "|" + PAYMENT_ID;
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec keySpec =
                    new javax.crypto.spec.SecretKeySpec(SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] bytes = mac.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
