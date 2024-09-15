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

import com.product.entity.Product;
import com.product.repository.ProductRepo;

@RestController
public class ProductController {

	@Autowired
	private ProductRepo repo;
	
	@GetMapping("/getProducts")
	public List<Product> getAllProducts()
	{
		return repo.findAll();
	}
	
	@GetMapping("/getProducts2")
	public Product[] getAllProduct2()
	{
		List<Product> all = repo.findAll();
		Product[] allp=all.toArray(new Product[all.size()]);
		return allp;
	}
	
	
	
	
	@GetMapping("/getProduct/{productId}")
	public Optional<Product> getProduct(@PathVariable Long productId)
	{
		
		return repo.findById(productId);  
		 
	}
	
	
	@PostMapping("/createProduct")
	public ResponseEntity<String> createProduct(@RequestBody Product product)
	{
		repo.save(product);
		return ResponseEntity.ok("Creation Successfull");
	}
	
	
	@PutMapping("/updateProduct/{productId}")
	public ResponseEntity<String> updateProduct(@RequestBody Product product,@PathVariable Long productId)
	{
		
		try
		{
			Optional<Product> byId = repo.findById(productId);
			Product product2 = byId.get();
			product2.setProductName(product.getProductName());
			product2.setDiscount_applied(product.isDiscount_applied());
			product2.setNet_quantity_instock(product.getNet_quantity_instock());
			product2.setPrice(product.getPrice());
			product2.setStates(product.getStates());
			product2.setCategory(product.getCategory());
			repo.save(product2);
			return ResponseEntity.ofNullable("Update Successfull");
		}
		catch(Exception e)
		{
			return ResponseEntity.ofNullable("Product not found");
		}

	}
	
	
	@DeleteMapping("/deleteProduct/{productId}")
	public ResponseEntity<String> deleteProduct(@PathVariable Long productId)
	{
		repo.deleteById(productId);
		return ResponseEntity.ok("Delete Successfull");
	}
	
	
	@GetMapping("/getname/{productId}")
	public String getProductName(@PathVariable Long productId)
	{
		String productName = repo.findById(productId).get().getProductName();
		System.out.println(productName);
		return productName;
		//return repo.findById(productId).get().getProductName();
	}
	
	@GetMapping("/getstates/{productId}")
	public List<String> getProductStates(@PathVariable Long productId)
	{
		return repo.findById(productId).get().getStates();
	}
	
	@GetMapping("/getdesc/{productId}")
	public String getProductDesc(@PathVariable Long productId)
	{
		return repo.findById(productId).get().getProductDesc();
	}
	
	
	@GetMapping("/getimage/{productId}")
	public String getProductImage(@PathVariable Long productId)
	{
		return repo.findById(productId).get().getImagepath();
	}
	
	
	
	
	
}
