package com.user.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.user.entity.UserCart;
import com.user.repository.UserCartRepo;

@RestController
public class UserCartController {

	@Autowired
	private UserCartRepo repo;
	
	@GetMapping("/getcart")
	public List<UserCart> getCart()
	{
		return repo.findAll();
	}
	
	@GetMapping("/getcartbyuid/{userId}")
	public UserCart[] getCartByUId(@PathVariable Long userId)
	{
		return repo.findByUserId(userId);
	}
	
	@DeleteMapping("/removefromcart/{cartId}")
	public ResponseEntity<String> removeFromCart(@PathVariable Long cartId)
	{
		repo.deleteById(cartId);
		return ResponseEntity.ok("Removed From Cart");
	}
	
//	@DeleteMapping("/removefromcart")
//	public ResponseEntity<String> removeFromCart1(@RequestBody UserCart cart)
//	{
//		repo.deleteByProductIdAndUserId(cart.getProductId(), cart.getUserId());
//		return ResponseEntity.ok("Removed From Cart");
//	}
	
	@PostMapping("/addtocart")
	public ResponseEntity<String> addToCart(@RequestBody UserCart cart)
	{
		try
		{
			UserCart usercart = repo.findByProductIdAndUserId(cart.getProductId(), cart.getCartId()).get();
			int qun = usercart.getQuantity()+cart.getQuantity();
			if(qun<0)
				qun=0;
			
			usercart.setQuantity(qun);
			usercart.setGst(cart.getGst());
			usercart.setTotal(qun * usercart.getPrice());
			repo.save(usercart);
			
			return ResponseEntity.ok("Product Quantity Updated");	
		}
		catch(Exception e)
		{
			repo.save(cart);
			return ResponseEntity.ok("Product Added to Cart");
		}
		
		
	}
	
	
	
	
	
}
