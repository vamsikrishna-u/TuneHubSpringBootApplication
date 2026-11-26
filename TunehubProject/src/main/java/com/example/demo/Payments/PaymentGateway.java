package com.example.demo.Payments;

public interface PaymentGateway {
	RazorpayOrder createOrder(long amount, String currency, String receipt);
	boolean verifySignature(String orderId, String paymentId, String signature);
	RazorpayPayment fetchPayment(String paymentId);

	record RazorpayOrder(String id, long amount, String currency) { }
	record RazorpayPayment(String id, String orderId, long amount, String currency, String status) { }
}
