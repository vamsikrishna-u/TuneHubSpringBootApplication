package com.example.demo.Payments;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Entities.PaymentOrder;
import com.example.demo.Entities.PaymentOrderStatus;
import com.example.demo.Repositories.PaymentOrderRepository;
import com.example.demo.Repositories.UserRepositories;

@Service
public class PaymentActivationService implements PremiumActivator {

	private final PaymentOrderRepository paymentOrderRepository;
	private final UserRepositories userRepositories;

	public PaymentActivationService(PaymentOrderRepository paymentOrderRepository,
			UserRepositories userRepositories) {
		this.paymentOrderRepository = paymentOrderRepository;
		this.userRepositories = userRepositories;
	}

	@Transactional
	@Override
	public PaymentVerificationStatus activatePremium(String orderId, String customerEmail, String paymentId) {
		Optional<PaymentOrder> result = paymentOrderRepository.findByRazorpayOrderIdForUpdate(orderId);
		if (result.isEmpty() || !result.get().getCustomer().getEmailId().equals(customerEmail)) {
			return PaymentVerificationStatus.INVALID;
		}

		PaymentOrder paymentOrder = result.get();
		if (paymentOrder.getStatus() == PaymentOrderStatus.PAID) {
			return paymentId.equals(paymentOrder.getRazorpayPaymentId())
					? PaymentVerificationStatus.ALREADY_VERIFIED : PaymentVerificationStatus.INVALID;
		}
		if (paymentOrder.getStatus() != PaymentOrderStatus.CREATED) {
			return PaymentVerificationStatus.INVALID;
		}

		paymentOrder.setRazorpayPaymentId(paymentId);
		paymentOrder.setStatus(PaymentOrderStatus.PAID);
		paymentOrder.setPaidAt(Instant.now());
		paymentOrder.getCustomer().setPremium(true);
		paymentOrderRepository.save(paymentOrder);
		userRepositories.save(paymentOrder.getCustomer());
		return PaymentVerificationStatus.VERIFIED;
	}
}
