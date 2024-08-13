package com.user.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;

import com.user.entity.UserCart;

public interface UserCartRepo extends JpaRepository<UserCart, Long> {

	public Optional<UserCart> findByProductIdAndUserId(Long pId,Long uId);
	public UserCart[] findByUserId(Long uId);
	//public ResponseEntity<String> deleteByProductIdAndUserId(Long pId,Long uId);
	
}
