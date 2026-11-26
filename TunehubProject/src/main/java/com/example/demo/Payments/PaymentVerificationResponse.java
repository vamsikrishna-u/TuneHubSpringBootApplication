package com.example.demo.Payments;

public record PaymentVerificationResponse(boolean verified, String status, String message) {
}
