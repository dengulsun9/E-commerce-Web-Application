package com.view.pojo;

import lombok.Data;

@Data
public class CartEx {

	
	private Long cartId;
	private Long userId;
	private Long productId;
	private Integer quantity;
	private Double price; 
	private Double gst;
	private Double total;
	private String productName,productDesc,imagePath;
}
