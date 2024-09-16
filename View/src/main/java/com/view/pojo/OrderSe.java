package com.view.pojo;


import lombok.Data;


@Data
public class OrderSe {

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
	private String productName;
	private String productImage;
	private String razorpayOrderId;
}
