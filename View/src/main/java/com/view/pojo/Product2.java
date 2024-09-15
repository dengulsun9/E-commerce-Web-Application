package com.view.pojo;

import java.util.List;

import lombok.Data;


@Data
public class Product2 {

	
	private Long productId;
	private String productName;
	private String productDesc;
	private Integer net_quantity_instock;
	private String color;
	private Double price;
	private String category;
	private Double gst;
	private List<String> states;
	private boolean discount_applied;
	private String imagepath;
	
	
	
}
