package com.example.demo.Payments;

public interface PaymentProcessor {
	PaymentOrderResponse createOrder(String customerEmail);
	PaymentVerificationStatus verifyPayment(String customerEmail, PaymentVerificationRequest request);
}
