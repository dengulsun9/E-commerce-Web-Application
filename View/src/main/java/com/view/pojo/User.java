package com.view.pojo;

import lombok.Data;


@Data
public class User {

	private Long user_Id;
	private String username;
	private String password;
	private String address;
	private String city;
	private Integer pincode;
	private String state;
	private String country;
	
}
