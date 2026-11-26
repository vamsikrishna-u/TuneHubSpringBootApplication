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
import com.example.demo.Payments.PaymentGateway.RazorpayOrder;
import com.example.demo.Payments.PaymentGateway.RazorpayPayment;
import com.example.demo.Repositories.PaymentOrderRepository;
import com.example.demo.Repositories.UserRepositories;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

	@Mock
	private PaymentGateway razorpayGateway;
	@Mock
	private PaymentOrderRepository paymentOrderRepository;
	@Mock
	private UserRepositories userRepositories;
	@Mock
	private PremiumActivator paymentActivationService;

	private PaymentService paymentService;
	private Users customer;
	private PaymentOrder paymentOrder;

	@BeforeEach
	void setUp() {
		paymentService = new PaymentService(razorpayGateway, paymentOrderRepository, userRepositories,
				paymentActivationService, 50000);
		customer = new Users();
		customer.setEmailId("customer@example.com");
		customer.setRole("CUSTOMER");
		paymentOrder = new PaymentOrder("order_123", customer, 50000, "INR");
	}

	@Test
	void createsProviderOrderAndStoresItForAuthenticatedCustomer() {
		when(userRepositories.findByEmailId(customer.getEmailId())).thenReturn(customer);
		when(razorpayGateway.createOrder(org.mockito.ArgumentMatchers.eq(50000L),
				org.mockito.ArgumentMatchers.eq("INR"), org.mockito.ArgumentMatchers.argThat(receipt ->
						receipt != null && receipt.length() <= 40)))
			.thenReturn(new RazorpayOrder("order_123", 50000, "INR"));

		PaymentOrderResponse response = paymentService.createOrder(customer.getEmailId());

		assertThat(response).isEqualTo(new PaymentOrderResponse("order_123", 50000, "INR"));
		verify(paymentOrderRepository).save(org.mockito.ArgumentMatchers.argThat(order ->
				order.getCustomer() == customer && "order_123".equals(order.getRazorpayOrderId())));
	}

	@Test
	void validSignatureAndCapturedMatchingPaymentActivatePremium() {
		givenCreatedOrder();
		when(razorpayGateway.verifySignature("order_123", "pay_123", "valid-signature")).thenReturn(true);
		when(razorpayGateway.fetchPayment("pay_123"))
			.thenReturn(new RazorpayPayment("pay_123", "order_123", 50000, "INR", "captured"));
		when(paymentActivationService.activatePremium("order_123", customer.getEmailId(), "pay_123"))
			.thenReturn(PaymentVerificationStatus.VERIFIED);

		assertThat(paymentService.verifyPayment(customer.getEmailId(), request()))
			.isEqualTo(PaymentVerificationStatus.VERIFIED);
		verify(paymentActivationService).activatePremium("order_123", customer.getEmailId(), "pay_123");
	}

	@Test
	void invalidSignatureNeverFetchesPaymentOrActivatesPremium() {
		givenCreatedOrder();
		when(razorpayGateway.verifySignature("order_123", "pay_123", "bad-signature")).thenReturn(false);

		assertThat(paymentService.verifyPayment(customer.getEmailId(), request("bad-signature")))
			.isEqualTo(PaymentVerificationStatus.INVALID);
		verify(razorpayGateway, never()).fetchPayment("pay_123");
		verify(paymentActivationService, never()).activatePremium(org.mockito.ArgumentMatchers.anyString(),
				org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
	}

	@Test
	void unknownOrderIsRejectedBeforeCallingProvider() {
		when(paymentOrderRepository.findWithCustomerByRazorpayOrderId("order_missing"))
			.thenReturn(Optional.empty());

		assertThat(paymentService.verifyPayment(customer.getEmailId(),
				new PaymentVerificationRequest("order_missing", "pay_123", "signature")))
			.isEqualTo(PaymentVerificationStatus.INVALID);
		verify(razorpayGateway, never()).verifySignature(org.mockito.ArgumentMatchers.anyString(),
				org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
	}

	@Test
	void anotherCustomerCannotVerifyOrder() {
		givenCreatedOrder();

		assertThat(paymentService.verifyPayment("attacker@example.com", request()))
			.isEqualTo(PaymentVerificationStatus.INVALID);
		verify(razorpayGateway, never()).verifySignature(org.mockito.ArgumentMatchers.anyString(),
				org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
	}

	@Test
	void amountMismatchNeverActivatesPremium() {
		givenCreatedOrder();
		when(razorpayGateway.verifySignature("order_123", "pay_123", "valid-signature")).thenReturn(true);
		when(razorpayGateway.fetchPayment("pay_123"))
			.thenReturn(new RazorpayPayment("pay_123", "order_123", 100, "INR", "captured"));

		assertThat(paymentService.verifyPayment(customer.getEmailId(), request()))
			.isEqualTo(PaymentVerificationStatus.INVALID);
		verify(paymentActivationService, never()).activatePremium(org.mockito.ArgumentMatchers.anyString(),
				org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
	}

	@Test
	void wrongCurrencyOrOrderDetailsNeverActivatePremium() {
		givenCreatedOrder();
		when(razorpayGateway.verifySignature("order_123", "pay_123", "valid-signature")).thenReturn(true);
		when(razorpayGateway.fetchPayment("pay_123"))
			.thenReturn(new RazorpayPayment("pay_123", "different_order", 50000, "INR", "captured"));

		assertThat(paymentService.verifyPayment(customer.getEmailId(), request()))
			.isEqualTo(PaymentVerificationStatus.INVALID);
		verify(paymentActivationService, never()).activatePremium(org.mockito.ArgumentMatchers.anyString(),
				org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
	}

	@Test
	void currencyMismatchNeverActivatesPremium() {
		givenCreatedOrder();
		when(razorpayGateway.verifySignature("order_123", "pay_123", "valid-signature")).thenReturn(true);
		when(razorpayGateway.fetchPayment("pay_123"))
			.thenReturn(new RazorpayPayment("pay_123", "order_123", 50000, "USD", "captured"));

		assertThat(paymentService.verifyPayment(customer.getEmailId(), request()))
			.isEqualTo(PaymentVerificationStatus.INVALID);
		verify(paymentActivationService, never()).activatePremium(org.mockito.ArgumentMatchers.anyString(),
				org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
	}

	@Test
	void fetchedPaymentIdMustMatchRequestedId() {
		givenCreatedOrder();
		when(razorpayGateway.verifySignature("order_123", "pay_123", "valid-signature")).thenReturn(true);
		when(razorpayGateway.fetchPayment("pay_123"))
			.thenReturn(new RazorpayPayment("different_payment", "order_123", 50000, "INR", "captured"));

		assertThat(paymentService.verifyPayment(customer.getEmailId(), request()))
			.isEqualTo(PaymentVerificationStatus.INVALID);
		verify(paymentActivationService, never()).activatePremium(org.mockito.ArgumentMatchers.anyString(),
				org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
	}

	@Test
	void authorizedButUncapturedPaymentDoesNotActivatePremium() {
		givenCreatedOrder();
		when(razorpayGateway.verifySignature("order_123", "pay_123", "valid-signature")).thenReturn(true);
		when(razorpayGateway.fetchPayment("pay_123"))
			.thenReturn(new RazorpayPayment("pay_123", "order_123", 50000, "INR", "authorized"));

		assertThat(paymentService.verifyPayment(customer.getEmailId(), request()))
			.isEqualTo(PaymentVerificationStatus.NOT_CAPTURED);
		verify(paymentActivationService, never()).activatePremium(org.mockito.ArgumentMatchers.anyString(),
				org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
	}

	@Test
	void repeatedVerificationReturnsAlreadyVerifiedWithoutReapplyingBenefits() {
		givenCreatedOrder();
		when(razorpayGateway.verifySignature("order_123", "pay_123", "valid-signature")).thenReturn(true);
		when(razorpayGateway.fetchPayment("pay_123"))
			.thenReturn(new RazorpayPayment("pay_123", "order_123", 50000, "INR", "captured"));
		when(paymentActivationService.activatePremium("order_123", customer.getEmailId(), "pay_123"))
			.thenReturn(PaymentVerificationStatus.ALREADY_VERIFIED);

		assertThat(paymentService.verifyPayment(customer.getEmailId(), request()))
			.isEqualTo(PaymentVerificationStatus.ALREADY_VERIFIED);
	}

	@Test
	void providerFailureIsRetryableAndDoesNotActivatePremium() {
		givenCreatedOrder();
		when(razorpayGateway.verifySignature("order_123", "pay_123", "valid-signature")).thenReturn(true);
		when(razorpayGateway.fetchPayment("pay_123"))
			.thenThrow(new PaymentGatewayException(new RuntimeException("provider unavailable")));

		assertThat(paymentService.verifyPayment(customer.getEmailId(), request()))
			.isEqualTo(PaymentVerificationStatus.TEMPORARY_FAILURE);
		verify(paymentActivationService, never()).activatePremium(org.mockito.ArgumentMatchers.anyString(),
				org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
	}

	private void givenCreatedOrder() {
		when(paymentOrderRepository.findWithCustomerByRazorpayOrderId("order_123"))
			.thenReturn(Optional.of(paymentOrder));
	}

	private PaymentVerificationRequest request() {
		return request("valid-signature");
	}

	private PaymentVerificationRequest request(String signature) {
		return new PaymentVerificationRequest("order_123", "pay_123", signature);
	}
}
