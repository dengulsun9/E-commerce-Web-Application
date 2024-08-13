package com.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.product.entity.Order1;

public interface OrderRepo extends JpaRepository<Order1,Long>{

	public Order1[] findByUserId(Long userId);

}
