package com.example.demo.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Entities.PaymentOrder;

import jakarta.persistence.LockModeType;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

	@Query("select paymentOrder from PaymentOrder paymentOrder join fetch paymentOrder.customer "
			+ "where paymentOrder.razorpayOrderId = :orderId")
	@Transactional(readOnly = true)
	Optional<PaymentOrder> findWithCustomerByRazorpayOrderId(@Param("orderId") String orderId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select paymentOrder from PaymentOrder paymentOrder join fetch paymentOrder.customer "
			+ "where paymentOrder.razorpayOrderId = :orderId")
	Optional<PaymentOrder> findByRazorpayOrderIdForUpdate(@Param("orderId") String orderId);
}
