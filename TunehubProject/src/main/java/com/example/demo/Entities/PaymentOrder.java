package com.example.demo.Entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "payment_orders", indexes = {
		@Index(name = "idx_payment_order_customer", columnList = "customer_id") }, uniqueConstraints = {
				@UniqueConstraint(name = "uk_payment_order_gateway_id", columnNames = "razorpay_order_id"),
				@UniqueConstraint(name = "uk_payment_order_payment_id", columnNames = "razorpay_payment_id") })
public class PaymentOrder {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "razorpay_order_id", nullable = false, length = 64)
	private String razorpayOrderId;

	@Column(name = "razorpay_payment_id", length = 64)
	private String razorpayPaymentId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "customer_id", nullable = false)
	private Users customer;

	@Column(nullable = false)
	private long amount;

	@Column(nullable = false, length = 3)
	private String currency;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private PaymentOrderStatus status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	@Column(name = "paid_at")
	private Instant paidAt;

	protected PaymentOrder() {
	}

	public PaymentOrder(String razorpayOrderId, Users customer, long amount, String currency) {
		this.razorpayOrderId = razorpayOrderId;
		this.customer = customer;
		this.amount = amount;
		this.currency = currency;
		this.status = PaymentOrderStatus.CREATED;
	}

	public Long getId() {
		return id;
	}

	public String getRazorpayOrderId() {
		return razorpayOrderId;
	}

	public String getRazorpayPaymentId() {
		return razorpayPaymentId;
	}

	public void setRazorpayPaymentId(String razorpayPaymentId) {
		this.razorpayPaymentId = razorpayPaymentId;
	}

	public Users getCustomer() {
		return customer;
	}

	public long getAmount() {
		return amount;
	}

	public String getCurrency() {
		return currency;
	}

	public PaymentOrderStatus getStatus() {
		return status;
	}

	public void setStatus(PaymentOrderStatus status) {
		this.status = status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getPaidAt() {
		return paidAt;
	}

	public void setPaidAt(Instant paidAt) {
		this.paidAt = paidAt;
	}
}
