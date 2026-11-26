package com.example.demo.Payments;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;

@Component
public class RazorpayPaymentGateway implements PaymentGateway {

	private final RazorpayClient client;
	private final String keySecret;

	public RazorpayPaymentGateway(@Value("${razorpay.key-id}") String keyId,
			@Value("${razorpay.key-secret}") String keySecret) throws RazorpayException {
		this.client = new RazorpayClient(keyId, keySecret);
		this.keySecret = keySecret;
	}

	@Override
	public PaymentGateway.RazorpayOrder createOrder(long amount, String currency, String receipt) {
		try {
			JSONObject request = new JSONObject();
			request.put("amount", amount);
			request.put("currency", currency);
			request.put("receipt", receipt);
			Order order = client.orders.create(request);
			return new PaymentGateway.RazorpayOrder(order.get("id"), ((Number) order.get("amount")).longValue(),
					order.get("currency"));
		} catch (RazorpayException | RuntimeException exception) {
			throw new PaymentGatewayException(exception);
		}
	}

	@Override
	public boolean verifySignature(String orderId, String paymentId, String signature) {
		try {
			JSONObject attributes = new JSONObject();
			attributes.put("razorpay_order_id", orderId);
			attributes.put("razorpay_payment_id", paymentId);
			attributes.put("razorpay_signature", signature);
			return Utils.verifyPaymentSignature(attributes, keySecret);
		} catch (RazorpayException | RuntimeException exception) {
			throw new PaymentGatewayException(exception);
		}
	}

	@Override
	public PaymentGateway.RazorpayPayment fetchPayment(String paymentId) {
		try {
			Payment payment = client.payments.fetch(paymentId);
			return new PaymentGateway.RazorpayPayment(payment.get("id"), payment.get("order_id"),
					((Number) payment.get("amount")).longValue(), payment.get("currency"), payment.get("status"));
		} catch (RazorpayException | RuntimeException exception) {
			throw new PaymentGatewayException(exception);
		}
	}
}
