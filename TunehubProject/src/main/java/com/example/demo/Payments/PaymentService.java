package com.example.demo.Payments;

import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.example.demo.Entities.PaymentOrder;
import com.example.demo.Entities.PaymentOrderStatus;
import com.example.demo.Entities.Users;
import com.example.demo.Repositories.PaymentOrderRepository;
import com.example.demo.Repositories.UserRepositories;
import com.example.demo.Payments.PaymentGateway.RazorpayOrder;
import com.example.demo.Payments.PaymentGateway.RazorpayPayment;

@Service
public class PaymentService implements PaymentProcessor {

	private final PaymentGateway razorpayGateway;
	private final PaymentOrderRepository paymentOrderRepository;
	private final UserRepositories userRepositories;
	private final PremiumActivator paymentActivationService;
	private final long premiumAmount;
	private static final String CURRENCY = "INR";

	public PaymentService(PaymentGateway razorpayGateway, PaymentOrderRepository paymentOrderRepository,
			UserRepositories userRepositories, PremiumActivator paymentActivationService,
			@Value("${tunehub.premium.amount:50000}") long premiumAmount) {
		this.razorpayGateway = razorpayGateway;
		this.paymentOrderRepository = paymentOrderRepository;
		this.userRepositories = userRepositories;
		this.paymentActivationService = paymentActivationService;
		this.premiumAmount = premiumAmount;
	}

	@Override
	public PaymentOrderResponse createOrder(String customerEmail) {
		Users customer = userRepositories.findByEmailId(customerEmail);
		if (customer == null) {
			throw new IllegalStateException("Authenticated customer account is unavailable");
		}

		String receipt = "th-" + UUID.randomUUID();
		RazorpayOrder razorpayOrder = razorpayGateway.createOrder(premiumAmount, CURRENCY, receipt);
		if (razorpayOrder.id() == null || razorpayOrder.id().isBlank()
				|| razorpayOrder.amount() != premiumAmount || !CURRENCY.equals(razorpayOrder.currency())) {
			throw new PaymentGatewayException(new IllegalStateException("Unexpected order details"));
		}

		PaymentOrder paymentOrder = new PaymentOrder(razorpayOrder.id(), customer, premiumAmount, CURRENCY);
		paymentOrderRepository.save(paymentOrder);
		return new PaymentOrderResponse(razorpayOrder.id(), premiumAmount, CURRENCY);
	}

	@Override
	public PaymentVerificationStatus verifyPayment(String customerEmail, PaymentVerificationRequest request) {
		if (request == null || isBlank(request.orderId()) || isBlank(request.paymentId())
				|| isBlank(request.signature())) {
			return PaymentVerificationStatus.INVALID;
		}

		PaymentOrder paymentOrder = paymentOrderRepository
				.findWithCustomerByRazorpayOrderId(request.orderId()).orElse(null);
		if (paymentOrder == null || !paymentOrder.getCustomer().getEmailId().equals(customerEmail)) {
			return PaymentVerificationStatus.INVALID;
		}
		if (paymentOrder.getStatus() == PaymentOrderStatus.PAID) {
			return request.paymentId().equals(paymentOrder.getRazorpayPaymentId())
					? PaymentVerificationStatus.ALREADY_VERIFIED : PaymentVerificationStatus.INVALID;
		}

		try {
			if (!razorpayGateway.verifySignature(request.orderId(), request.paymentId(), request.signature())) {
				return PaymentVerificationStatus.INVALID;
			}
		} catch (PaymentGatewayException exception) {
			return PaymentVerificationStatus.TEMPORARY_FAILURE;
		}

		RazorpayPayment payment;
		try {
			payment = razorpayGateway.fetchPayment(request.paymentId());
		} catch (PaymentGatewayException exception) {
			return PaymentVerificationStatus.TEMPORARY_FAILURE;
		}

		if (payment == null || payment.id() == null || payment.orderId() == null
				|| payment.currency() == null || payment.status() == null
				|| !request.paymentId().equals(payment.id()) || !request.orderId().equals(payment.orderId())
				|| payment.amount() != paymentOrder.getAmount()
				|| !paymentOrder.getCurrency().equals(payment.currency())) {
			return PaymentVerificationStatus.INVALID;
		}
		if (!"captured".equals(payment.status().toLowerCase(Locale.ROOT))) {
			return PaymentVerificationStatus.NOT_CAPTURED;
		}

		try {
			return paymentActivationService.activatePremium(request.orderId(), customerEmail, request.paymentId());
		} catch (DataIntegrityViolationException exception) {
			return PaymentVerificationStatus.INVALID;
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
