package com.example.demo.Payments;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;

class RazorpayPaymentGatewayTest {

	@Test
	void verifiesOfficialOrderPaymentSignature() throws Exception {
		String secret = "test-secret";
		String signature = hmacSha256("order_test|payment_test", secret);
		RazorpayPaymentGateway gateway = new RazorpayPaymentGateway("test-key", secret);

		assertThat(gateway.verifySignature("order_test", "payment_test", signature)).isTrue();
	}

	@Test
	void rejectsInvalidPaymentSignature() throws Exception {
		RazorpayPaymentGateway gateway = new RazorpayPaymentGateway("test-key", "test-secret");

		assertThat(gateway.verifySignature("order_test", "payment_test", "invalid-signature")).isFalse();
	}

	private String hmacSha256(String payload, String secret) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
		return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
	}
}
