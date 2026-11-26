package com.example.demo.Controller;

import java.security.Principal;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.Payments.PaymentErrorResponse;
import com.example.demo.Payments.PaymentGatewayException;
import com.example.demo.Payments.PaymentOrderResponse;
import com.example.demo.Payments.PaymentProcessor;
import com.example.demo.Payments.PaymentVerificationRequest;
import com.example.demo.Payments.PaymentVerificationResponse;
import com.example.demo.Payments.PaymentVerificationStatus;

@Controller
public class PaymentController {

	private final PaymentProcessor paymentService;

	public PaymentController(PaymentProcessor paymentService) {
		this.paymentService = paymentService;
	}

	@PostMapping("/createOrder")
	@ResponseBody
	public ResponseEntity<?> createOrder(Principal principal) {
		try {
			PaymentOrderResponse order = paymentService.createOrder(principal.getName());
			return ResponseEntity.ok(order);
		} catch (PaymentGatewayException | DataAccessException exception) {
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
					.body(new PaymentErrorResponse("Payment service is temporarily unavailable."));
		}
	}

	@PostMapping("/verify")
	@ResponseBody
	public ResponseEntity<PaymentVerificationResponse> verifyPayment(
			Principal principal, @RequestBody PaymentVerificationRequest request) {
		PaymentVerificationStatus status;
		try {
			status = paymentService.verifyPayment(principal.getName(), request);
		} catch (PaymentGatewayException | DataAccessException exception) {
			status = PaymentVerificationStatus.TEMPORARY_FAILURE;
		}

		return switch (status) {
			case VERIFIED -> ResponseEntity.ok(new PaymentVerificationResponse(true, "verified",
					"Payment verified. Premium access is active."));
			case ALREADY_VERIFIED -> ResponseEntity.ok(new PaymentVerificationResponse(true, "already_verified",
					"This payment has already been verified."));
			case NOT_CAPTURED -> ResponseEntity.status(HttpStatus.CONFLICT)
					.body(new PaymentVerificationResponse(false, "not_captured",
							"Payment is not captured yet. Please wait and retry."));
			case TEMPORARY_FAILURE -> ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
					.body(new PaymentVerificationResponse(false, "temporarily_unavailable",
							"Payment verification is temporarily unavailable. Please retry."));
			case INVALID -> ResponseEntity.badRequest()
					.body(new PaymentVerificationResponse(false, "invalid",
							"Payment could not be verified. Please check the payment or contact support."));
		};
	}

	/** Compatibility route only; premium is activated by verified payment processing, never by navigation. */
	@GetMapping("/payment-success")
	public String paymentSuccess() {
		return "redirect:/showallsongs";
	}

	/** Cancellation and failure leave the customer's premium state unchanged. */
	@GetMapping("/payment-failure")
	public String paymentFail() {
		return "redirect:/showallsongs";
	}
}
