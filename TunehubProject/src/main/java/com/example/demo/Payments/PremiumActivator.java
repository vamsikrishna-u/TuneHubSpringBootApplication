package com.example.demo.Payments;

public interface PremiumActivator {
	PaymentVerificationStatus activatePremium(String orderId, String customerEmail, String paymentId);
}
