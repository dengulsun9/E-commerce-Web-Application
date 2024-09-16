package com.product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.product.entity.Order1;

public interface OrderRepo extends JpaRepository<Order1,Long>{

	public Order1[] findByUserId(Long userId);
	
	//public Order1 findByRazorpayOrderId(String orderId); 
	
	public List<Order1> findByRazorpayOrderId(String orderId);

}
