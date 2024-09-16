package com.product.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Order1 {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long orderId;
	private String dateTime;
	private String shippingDate;
	private String deliveryDate;
	private Long userId;
	private String orderStatus;
	private Long productId;
	private Integer orderQuantity;
	private Double totalPrice;
	private String shippingAddress;
	private String razorpayOrderId;
	
}
