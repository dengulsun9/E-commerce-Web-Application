package com.product.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.product.entity.Order1;
import com.product.misc.Status;
import com.product.repository.OrderRepo;

@RestController
public class OrderController {

	
	@Autowired
	private OrderRepo repo;
	
	@PostMapping("/CreateOrder")
	public ResponseEntity<String> createOrder(@RequestBody Order1 order)
	{
		repo.save(order);
		return ResponseEntity.ok("Order Create Success");
	}
	
	
	@DeleteMapping("/DeleteProduct/{orderId}")
	public ResponseEntity<String> removeOrder(@PathVariable Long orderId)
	{
		try
		{
			Order1 order = repo.findById(orderId).get();
			repo.delete(order);
			
			return ResponseEntity.ok("Order Removed Successfully");
			
		}
		catch(Exception e)
		{
			return ResponseEntity.ok("Order not found");
		}
	}
	
	@GetMapping("/order/{orderId}")
	public Optional<Order1> getOrder(@PathVariable Long orderId)
	{
		return repo.findById(orderId);
	}
	
	
	@GetMapping("/orderStatus/{orderId}")
	public String getStatus(@PathVariable Long orderId)
	{
		try
		{
			return repo.findById(orderId).get().getOrderStatus();
		}
		catch(Exception e)
		{
			return "Order Not Found";
		}
	}
	
	@GetMapping("/getOrders")
	public List<Order1> getOrders()
	{
		return repo.findAll();
	}
	
	
	@GetMapping("/getorderbyuid/{userId}")
	public Order1[] getOrderbyUId(@PathVariable Long userId)
	{
		return repo.findByUserId(userId);
	}
	
	@PutMapping("/updatestatus/{orderId}")
	public ResponseEntity<String> updateStatus(@PathVariable Long orderId)
	{
		Order1 order = repo.findById(orderId).get();
		order.setOrderStatus(Status.CANCELED.toString());
		repo.save(order);
		return ResponseEntity.ok("order cancelled");
	}
	
	
}
