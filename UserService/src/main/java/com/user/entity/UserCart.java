package com.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class UserCart {

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long cartId;
	private Long userId;
	private Long productId;
	private Integer quantity;
	private Double price;
	private Double gst;
	private Double total;
	private String productName,productDesc,imagePath;
	
}
