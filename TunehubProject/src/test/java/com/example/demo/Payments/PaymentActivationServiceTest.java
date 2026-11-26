package com.example.demo.Payments;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.Entities.PaymentOrder;
import com.example.demo.Entities.Users;
import com.example.demo.Repositories.PaymentOrderRepository;
import com.example.demo.Repositories.UserRepositories;

@ExtendWith(MockitoExtension.class)
class PaymentActivationServiceTest {

	@Mock
	private PaymentOrderRepository paymentOrderRepository;
	@Mock
	private UserRepositories userRepositories;

	private PaymentActivationService activationService;
	private Users customer;
	private PaymentOrder paymentOrder;

	@BeforeEach
	void setUp() {
		activationService = new PaymentActivationService(paymentOrderRepository, userRepositories);
		customer = new Users();
		customer.setEmailId("customer@example.com");
		paymentOrder = new PaymentOrder("order_123", customer, 50000, "INR");
		when(paymentOrderRepository.findByRazorpayOrderIdForUpdate("order_123"))
			.thenReturn(Optional.of(paymentOrder));
	}

	@Test
	void activatesPremiumOnceAndReturnsAlreadyVerifiedForRepeat() {
		assertThat(activationService.activatePremium("order_123", customer.getEmailId(), "pay_123"))
			.isEqualTo(PaymentVerificationStatus.VERIFIED);
		assertThat(activationService.activatePremium("order_123", customer.getEmailId(), "pay_123"))
			.isEqualTo(PaymentVerificationStatus.ALREADY_VERIFIED);

		assertThat(customer.isPremium()).isTrue();
		verify(paymentOrderRepository).save(paymentOrder);
		verify(userRepositories).save(customer);
	}

	@Test
	void orderCannotActivatePremiumForDifferentCustomer() {
		assertThat(activationService.activatePremium("order_123", "other@example.com", "pay_123"))
			.isEqualTo(PaymentVerificationStatus.INVALID);
		assertThat(customer.isPremium()).isFalse();
		verify(paymentOrderRepository, never()).save(paymentOrder);
		verify(userRepositories, never()).save(customer);
	}
}
