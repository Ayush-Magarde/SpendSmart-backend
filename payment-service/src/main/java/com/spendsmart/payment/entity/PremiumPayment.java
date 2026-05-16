package com.spendsmart.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "premium_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PremiumPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String userEmail;

    private Boolean isPremium;

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
