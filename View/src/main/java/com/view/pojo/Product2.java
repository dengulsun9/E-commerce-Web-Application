package com.view.pojo;

import lombok.Data;


@Data
public class Product2 {

	
	private Long productId;
	private String productName;
	private String productDesc;
	private Integer net_quantity_instock;
	private String color;
	private Double price;
	private boolean discount_applied;
	private String imagepath;
	
	
	
}
