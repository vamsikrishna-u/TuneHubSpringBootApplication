package com.example.demo.Payments;

public record PaymentVerificationRequest(String orderId, String paymentId, String signature) {
}
