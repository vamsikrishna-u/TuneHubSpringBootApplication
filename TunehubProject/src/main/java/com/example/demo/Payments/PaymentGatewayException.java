package com.example.demo.Payments;

public class PaymentGatewayException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public PaymentGatewayException(Throwable cause) {
		super("Payment provider request failed", cause);
	}
}
