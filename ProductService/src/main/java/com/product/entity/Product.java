package com.product.entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long productId;
	private String productName;
	private String productDesc;
	private Integer net_quantity_instock;
	private String color;
	private String category;
	private Double price;
	private List<String> states;
	private boolean discount_applied;
	private String imagepath;
	
	
	
}
