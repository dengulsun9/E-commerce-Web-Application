package com.view.pojo;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;

@JsonPOJOBuilder
@Data
public class LogData {

	
	private Long user_Id;
	private String username;
	private String password;
	private String address;
	private String city;
	private Integer pincode;
	private String state;
	private String country;
}
