package com.example.demo.Payments;

public record PaymentOrderResponse(String orderId, long amount, String currency) {
}
