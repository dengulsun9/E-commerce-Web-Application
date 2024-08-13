package com.user.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.user.entity.UserData;
import com.user.repository.UserDataRepo;

import ch.qos.logback.core.status.Status;

@RestController
public class UserDataController {

	@Autowired
	private UserDataRepo repo;
	
	
	@GetMapping("/allUsers")
	public List<UserData> getAllUsers()
	{
		return repo.findAll();
	}
	
	@GetMapping("/allUsers2")
	public UserData[] getAll()
	{
		List<UserData> all = repo.findAll();
		UserData[] data=all.toArray(new UserData[all.size()]);
		return data;
	}
	
	@GetMapping("/getUserDetails/{id}")
	public Optional<UserData> getUserDetails(@PathVariable Long id)
	{
	    return repo.findById(id);
	}

	
	@PostMapping("/addUser")
	public ResponseEntity<String> addUserData(@RequestBody UserData userdata)
	{
		repo.save(userdata);
		return new ResponseEntity<String>("User Added Successfully",HttpStatus.CREATED);
		
	}
	
	@DeleteMapping("/user/{userid}")
	public ResponseEntity<String> deleteUserById(@PathVariable Long userid)
	{
		repo.deleteById(userid);
		return ResponseEntity.ok("Deleted Successfully");
	}
	
	
	@GetMapping("getUser/{username}/{password}")
	public Optional<UserData> getUser(@PathVariable String username,@PathVariable String password )
	{	
		
		try
		{
		    return repo.findByUsernameAndPassword(username, password);
		}
		catch(Exception e)
		{
			System.out.println("enterig catch");
			return null;
		}
	}
	
	@GetMapping("/getbyid/{uid}")
	public Optional<UserData> getById(@PathVariable Long uid)
	{
		return repo.findById(uid);
	}
	
	
	@PutMapping("/update/{userid}")
	public ResponseEntity<String> updateCity(@RequestBody UserData userdata,@PathVariable Long userid)
	{
		Optional<UserData> byId = repo.findById(userid);
		UserData userData2 = byId.get();
		userData2.setCity(userdata.getCity());
		userData2.setAddress(userdata.getAddress());
		userData2.setPincode(userdata.getPincode());
		repo.save(userData2);
		return ResponseEntity.ok("Update Success");
	}
	
	
	
}
