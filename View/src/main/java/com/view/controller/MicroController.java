package com.view.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.view.misc.Status;
import com.view.pojo.CartEx;
import com.view.pojo.LogData;
import com.view.pojo.LoginData;
import com.view.pojo.OrderSe;
import com.view.pojo.Product2;
import com.view.pojo.UpdateData;
import com.view.pojo.User;
import com.view.pojo.UserData;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class MicroController {

	RestTemplate rt=new RestTemplate();
	HttpSession session;
	
	@Value("${razorpay.key.id}")
	private String razorPayKey;
	
	@Value("${razorpay.secret.key}")
	private String razorPaySecret;
	
	@GetMapping("/login")
	public String login(Model model)
	{
		if(session!=null)
		{
			return "redirect:/getallproducts";
		}
		else
		{
			LoginData ld=new LoginData();
			model.addAttribute("loginData", ld);
			return "login";
			
			
		}
	}
	
	@PostMapping("/getLogin")
	public String getLogin(@ModelAttribute LoginData logindata,HttpServletRequest req,HttpServletResponse res)
	{
		String uname = logindata.getUsername();
		String pword = logindata.getPassword();
		
		
		LogData log = rt.getForObject("http://localhost:8081/getUser/"+uname+"/"+pword, LogData.class);
		if(log==null)
		{
			System.out.println("not coming");
			return "login";
		}
		else
		{
			System.out.println("else coming");
			Long userId = log.getUser_Id();
			String userName = log.getUsername();
			session=req.getSession(true);
			session.setAttribute("userName", userName);
			session.setAttribute("userId", userId);
			Cookie cookie=new Cookie("SID",session.getId());
			cookie.setMaxAge(-1);
			cookie.setHttpOnly(true);
			cookie.setPath("/");
			res.addCookie(cookie);
			return "redirect:/getallproducts";
		}
	}
	
	
	@GetMapping("/register")
	public String register(Model model)
	{
		UserData ud=new UserData();
		model.addAttribute("userData", ud);
		return "register";
	}
	
	@PostMapping("/adduser")
	public String addUser(@ModelAttribute UserData userdata)
	{
		ResponseEntity<String> res = rt.postForEntity("http://localhost:8081/addUser", userdata, String.class);
		if(res.getBody().equals("Insert Successfull"))
		{
			return "redirect:/login";
		}
		else
		{
			return "redirect:/register";
		}
	}
	
	
	@GetMapping("/allUsers")
	public String getAllUsers(Model model)
	{
		if(session==null)
		{
			return "redirect:/login";
		}
		else
		{
			String endpointUrl="http://localhost:8081/allUsers";
			ResponseEntity<List<User>> response = rt.exchange(endpointUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<User>>() {
			});
			List<User> userDataList = response.getBody();
			model.addAttribute("logData", userDataList);
			
			return "allUser";
		}
	}
	
	@GetMapping("/allUsers2")
	public String getAll(Model model)
	{
		if(session==null)
		{
			return "redirect:/login";
		}
		else
		{
			User[] forObject = rt.getForObject("http://localhost:8081/allUsers2", User[].class);
			model.addAttribute("logData", forObject);
			return "allUser";
		}
		
	}
	
	@PostMapping("/user/{userId}")
	public String deleteUser(@PathVariable Long userId)
	{
		rt.delete("http://localhost:8081/user/"+userId);
		return "redirect://allUsers2";
	}
	
	@GetMapping("/user/edit/{userId}")
	public String update(Model model,@PathVariable Long userId)
	{
		if(session==null)
		{
			return "redirect://login";
		}
		else
		{
			UpdateData ud=new UpdateData();
			ud.setUser_id(userId);
			model.addAttribute("UpdateData",ud);
			return "update";	
		}
	}
	
	@PostMapping("/getUpdated/{userId}")
	public String getUpdated(@ModelAttribute UpdateData updatedata,@PathVariable Long userId)
	{
		rt.put("http://localhost:8081/update/"+userId, updatedata);
		return "redirect://allUsers2";
	}
	
	
	@GetMapping("/getallproducts")
	public String getAllProducts(Model model)
	{
		if(session==null)
		{
			return "redirect:/login";
		}
		else
		{
			Product2[] forObject = rt.getForObject("http://localhost:8082/getProducts2", Product2[].class);
			model.addAttribute("products",forObject);
			return "products";
			
		}
	}
	
	
	private double getGstRateForCategory(String category) {
		double gstRate;
		switch (category) {
		case "Electronics": {
			gstRate = 0.18;
			break;
		}
		case "Clothing": {
			gstRate = 0.12;
			break;
		}
		case "Vehicle": {
			gstRate = 0.20;
			break;
		}
		default: {
			gstRate = 0.05;
			break;
		}
		}
		return gstRate;
	}
	
	@PostMapping("/addtocart/{productId}/{quantity}/{price}")
	public String addToCart(@PathVariable Long productId,@PathVariable Integer quantity,@PathVariable Double price,Model model)
	{
		Long userId=(Long)session.getAttribute("userId");
		User user = rt.getForObject("http://localhost:8081/getbyid/"+userId, User.class);
		String userState = user.getState();
		
		Product2 product = rt.getForObject("http://localhost:8082/getProduct/"+productId, Product2.class);
		List<String> productStates = product.getStates();
		
		if(productStates.contains(userState))
		{
			CartEx cartex=new CartEx();
			cartex.setProductId(productId);
			cartex.setQuantity(quantity);
			cartex.setUserId(userId);
			double gstRateForCategory = getGstRateForCategory(product.getCategory());
			double gstAmount = product.getPrice() * gstRateForCategory;
			product.setGst(gstAmount);
			double totalPrice = price + product.getGst();
			cartex.setPrice(price);
			cartex.setGst(product.getGst());
			cartex.setTotal(totalPrice);
			cartex.setImagePath(rt.getForEntity("http://localhost:8082/getimage/"+productId, String.class).getBody());
			cartex.setProductName(rt.getForEntity("http://localhost:8082/getname/"+productId, String.class).getBody());
			cartex.setProductDesc(rt.getForEntity("http://localhost:8082/getdesc/"+productId, String.class).getBody());
			rt.postForEntity("http://localhost:8081/addtocart", cartex, String.class);
			 Product2[] products = rt.getForObject("http://localhost:8082/getProducts2", Product2[].class);
		      model.addAttribute("products", products);
		      model.addAttribute("successMessage", "Product added to cart successfully.");
		}
		else
		{
			 Product2[] products = rt.getForObject("http://localhost:8082/getProducts2", Product2[].class);
		        model.addAttribute("products", products);
		        model.addAttribute("errorMessage", "Product not available in your state.");
		}
		return "products";
		
	}
	
	
	@PostMapping("/placeorder/{productId}/{quantity}/{price}")
	public String placeOrder(@PathVariable Long productId, @PathVariable Integer quantity, @PathVariable Double price, Model model) {
	    Long userId = (Long) session.getAttribute("userId");
	    if (userId == null) {
	        model.addAttribute("errorMessage", "User not logged in.");
	        return "redirect:/getallproducts";
	    }

	    
	    User user = rt.getForObject("http://localhost:8081/getbyid/" + userId, User.class);
	    Product2 product = rt.getForObject("http://localhost:8082/getProduct/" + productId, Product2.class);

	 
	    String userState = user.getState();
	    List<String> productStates = product.getStates();

	    if (!productStates.contains(userState)) {
	        model.addAttribute("errorMessage", "Product not available in your state.");
	        return "redirect:/getallproducts";
	    }

	 
	    OrderSe order = new OrderSe();
	    order.setDateTime(LocalDate.now().toString());
	    order.setShippingDate(LocalDate.now().plusDays(2).toString());
	    order.setDeliveryDate(LocalDate.now().plusDays(4).toString());
	    order.setOrderQuantity(quantity);
	    order.setOrderStatus(Status.ORDER_CONFIRMED.toString());
	    order.setUserId(userId);
	    order.setProductId(productId);
	    
	    double gstAmount = product.getPrice() * getGstRateForCategory(product.getCategory());
	    order.setTotalPrice(price + gstAmount);

	   
	    String address = user.getAddress() + " , " + user.getCity() + " , " + user.getPincode();
	    order.setShippingAddress(address);

	    
	    try {
	        OrderSe createdOrder = rt.postForObject("http://localhost:8082//CreateOrder1", order, OrderSe.class);
	        order.setOrderId(createdOrder.getOrderId());
	        
	        //integreating with razorpay gateway
	        JSONObject orderReq=new JSONObject();
	        orderReq.put("amount", (price + gstAmount)*100);
	        orderReq.put("currency", "INR");
	        orderReq.put("receipt", user.getUsername());
	        
	        
	        RazorpayClient razorpay = new RazorpayClient(razorPayKey, razorPaySecret);
	        Order razorpayOrder = razorpay.orders.create(orderReq);
	        System.out.println(razorpayOrder);
	        
	       
	        
	        order.setRazorpayOrderId(razorpayOrder.get("id"));
	        order.setOrderStatus(Status.PAYMENT_PENDING.toString()); // Update to payment pending
	        rt.put("http://localhost:8082/updateOrder", order);
	        
	        model.addAttribute("razorpayKey", razorPayKey);
	        model.addAttribute("razorpayOrderId", razorpayOrder.get("id"));
	        model.addAttribute("amount", razorpayOrder.get("amount"));
	        model.addAttribute("user", user);
	        model.addAttribute("product", product);
	        
	        model.addAttribute("successMessage", "Order placed successfully!");
	        System.out.println("coming to end");
	        return "razorpayCheckout";
	    } catch (Exception e) {
	        model.addAttribute("errorMessage", "Error placing order. Please try again.");
	        return "redirect:/getallproducts";
	    }

	}

	
//	@GetMapping("/handle-payment-callback")
//	public String handlePaymentCallback(@RequestParam Map<String, String> respPayload, Model model) {
//	    // Your code here
//	    String razorPayOrderId = respPayload.get("razorpay_order_id");
//	    String razorPayPaymentId = respPayload.get("razorpay_payment_id");
//	    String razorPaySignature = respPayload.get("razorpay_signature");
//
//	    try {
//	        // Verify the signature here using Razorpay SDK (optional, but recommended)
//	        // If successful, update the order status to "Payment Completed"
//	        OrderSe order = rt.getForObject("http://localhost:8082/getOrderByRazorpayId/" + razorPayOrderId, OrderSe.class);
//	        System.out.println(order);
//	        order.setOrderStatus(Status.ORDER_CONFIRMED.toString());
//	        rt.put("http://localhost:8082/updateOrder", order);
//
//	        model.addAttribute("successMessage", "Payment successful! Your order has been confirmed.");
//	        return "orderSuccess";  // A success page after payment
//	    } catch (Exception e) {
//	        model.addAttribute("errorMessage", "Payment failed. Please try again.");
//	        System.out.println(e.getMessage());
//	        return "paymentFailure";  // A failure page after unsuccessful payment
//	    }
//	}

	
	@GetMapping("/handle-payment-callback")
	public String handlePaymentCallback(@RequestParam Map<String, String> respPayload, Model model) {
	    String razorPayOrderId = respPayload.get("razorpay_order_id");
	    String razorPayPaymentId = respPayload.get("razorpay_payment_id");
	    String razorPaySignature = respPayload.get("razorpay_signature");

	    try {
	        
	    	 ResponseEntity<List<OrderSe>> responseEntity = rt.exchange(
	    	            "http://localhost:8082/getAllOrdersByRazorpayId/" + razorPayOrderId,
	    	            HttpMethod.GET,
	    	            null,
	    	            new ParameterizedTypeReference<List<OrderSe>>() {}
	    	        );
	    	        List<OrderSe> orders = responseEntity.getBody();
	    	        
	        if (orders != null && !orders.isEmpty()) {
	           
	            for (OrderSe order : orders) {
	                order.setOrderStatus(Status.ORDER_CONFIRMED.toString());
	                rt.put("http://localhost:8082/updateOrder", order);
	            }

	            model.addAttribute("successMessage", "Payment successful! All related orders have been confirmed.");
	            return "orderSuccess"; 
	        } else {
	            model.addAttribute("errorMessage", "No orders found with the provided Razorpay order ID.");
	            return "paymentFailure";  
	        }
	    } catch (Exception e) {
	        model.addAttribute("errorMessage", "Payment failed. Please try again.");
	        System.out.println(e.getMessage());
	        return "paymentFailure"; 
	    }
	}

	
	
	@PostMapping("/addtocart2/{productId}/{quantity}")
	public String addToCart2(@PathVariable Long productId,@PathVariable Integer quantity)
	{
		CartEx cartEx=new CartEx();
		cartEx.setProductId(productId);
		cartEx.setQuantity(quantity);
		cartEx.setUserId((Long)(session.getAttribute("userId")));
		rt.postForEntity("http://localhost:8081/addtocart", cartEx, String.class);
		return "redirect:/getcart";
	}
	
	
	@GetMapping("/getcart")
	public String getCart(Model model, HttpSession session) {
	    Double sum = 0.0;

	    if (session == null) {
	        return "redirect:/login";
	    }

	    Long userId = (Long) session.getAttribute("userId");

	    if (userId == null) {
	        return "redirect:/login";
	    }

	    CartEx[] cartItems = rt.getForObject("http://localhost:8081/getcartbyuid/" + userId, CartEx[].class);
	    if (cartItems != null) {
	        for (CartEx item : cartItems) {
	            
	            if (item.getTotal() != null) {
	                sum += item.getTotal();
	            }
	        }
	    }
	    
	    model.addAttribute("cartvalue", cartItems);
	    model.addAttribute("sum", sum);
	    model.addAttribute("UserName", userId.toString()); 

	    return "cart";
	}


	
	@PostMapping("/removefromcart/{cartId}")
	public String removeFromCart(@PathVariable Long cartId)
	{
		rt.delete("http://localhost:8081/removefromcart/"+cartId);
		return "redirect:/getcart";
	}
	
	
	@GetMapping("/addproduct")
	public String addProduct(Model model)
	{
		Product2 product=new Product2();
		product.setDiscount_applied(true);
		model.addAttribute("product", product);
		return "addproduct";
	}
	
	@PostMapping("/addproductpost")
	public String addProductPost(@ModelAttribute Product2 product)
	{
		rt.postForObject("http://localhost:8082/createProduct", product, String.class);
		return "redirect:/addproduct";
		
	}
	
//	@PostMapping("/createorder")
//	public String createOrder()
//	{
//		CartEx[] forObject = rt.getForObject("http://localhost:8081/getcartbyuid/"+(Long)(session.getAttribute("userId")), CartEx[].class);
//		for(CartEx ct:forObject)
//		{
//			if(ct.getQuantity()==0)
//			{
//				rt.delete("http://localhost:8081/removefromcart/"+ct.getCartId());
//				continue;
//			}
//			else
//			{
//				OrderSe os=new OrderSe();
//				os.setDateTime(LocalDate.now().toString());
//				os.setShippingDate(LocalDate.now().plusDays(2).toString());
//				os.setDeliveryDate(LocalDate.now().plusDays(4).toString());
//				os.setOrderQuantity(ct.getQuantity());
//				os.setOrderStatus(Status.ORDER_CONFIRMED.toString());
//				os.setUserId((Long)(session.getAttribute("userId")));
//				os.setProductId(ct.getProductId());
//				os.setTotalPrice(ct.getTotal());
//				User ob = rt.getForObject("http://localhost:8081/getbyid/"+os.getUserId(), User.class);
//				os.setShippingAddress(ob.getAddress()+" , "+ob.getCity()+" , "+ob.getPincode());
//				rt.postForObject("http://localhost:8082/CreateOrder", os, String.class);	
//				rt.delete("http://localhost:8081/removefromcart/"+ct.getCartId());
//			}
//		}
//		return "redirect:/getcart";
//	}
	
//	@PostMapping("/createorder")
//	public String createOrder(Model model) throws Exception
//	{
//	    CartEx[] forObject = rt.getForObject("http://localhost:8081/getcartbyuid/"+(Long)(session.getAttribute("userId")), CartEx[].class);
//	    for(CartEx ct:forObject)
//	    {
//	        if(ct.getQuantity()==0)
//	        {
//	            rt.delete("http://localhost:8081/removefromcart/"+ct.getCartId());
//	            continue;
//	        }
//	        else
//	        {
//	            OrderSe os=new OrderSe();
//	            os.setDateTime(LocalDate.now().toString());
//	            os.setShippingDate(LocalDate.now().plusDays(2).toString());
//	            os.setDeliveryDate(LocalDate.now().plusDays(4).toString());
//	            os.setOrderQuantity(ct.getQuantity());
//	            os.setOrderStatus(Status.ORDER_CONFIRMED.toString());
//	            os.setUserId((Long)(session.getAttribute("userId")));
//	            os.setProductId(ct.getProductId());
//	            os.setTotalPrice(ct.getTotal());
//	            User ob = rt.getForObject("http://localhost:8081/getbyid/"+os.getUserId(), User.class);
//	            os.setShippingAddress(ob.getAddress()+" , "+ob.getCity()+" , "+ob.getPincode());
//	            
//	            // Integrate with Razorpay gateway
//	            JSONObject orderReq=new JSONObject();
//	            orderReq.put("amount", os.getTotalPrice()*100);
//	            orderReq.put("currency", "INR");
//	            orderReq.put("receipt", ob.getUsername());
//	            
//	            RazorpayClient razorpay = new RazorpayClient(razorPayKey, razorPaySecret);
//	            Order razorpayOrder = razorpay.orders.create(orderReq);
//	            System.out.println(razorpayOrder);
//	            
//	            os.setRazorpayOrderId(razorpayOrder.get("id"));
//	            os.setOrderStatus(Status.PAYMENT_PENDING.toString()); // Update to payment pending
//	            
//	            // Save the order with Razorpay order ID
//	            rt.postForObject("http://localhost:8082/CreateOrder", os, String.class);
//	            
//	            // Add attributes to the model for Razorpay checkout
//	            model.addAttribute("razorpayKey", razorPayKey);
//	            model.addAttribute("razorpayOrderId", razorpayOrder.get("id"));
//	            model.addAttribute("amount", razorpayOrder.get("amount"));
//	            model.addAttribute("user", ob);
//	            model.addAttribute("product", ct);
//	            
//	            model.addAttribute("successMessage", "Order placed successfully!");
//	            System.out.println("coming to end");
//	            return "razorpayCheckout";
//	        }
//	    }
//	    return "redirect:/getcart";
//	}
	
	
	@PostMapping("/createorder")
	public String createOrder(Model model) throws Exception
	{
	    CartEx[] forObject = rt.getForObject("http://localhost:8081/getcartbyuid/"+(Long)(session.getAttribute("userId")), CartEx[].class);
	    double totalAmount = 0;
	    for(CartEx ct:forObject)
	    {
	        if(ct.getQuantity()==0)
	        {
	            rt.delete("http://localhost:8081/removefromcart/"+ct.getCartId());
	            continue;
	        }
	        else
	        {
	            OrderSe os=new OrderSe();
	            os.setDateTime(LocalDate.now().toString());
	            os.setShippingDate(LocalDate.now().plusDays(2).toString());
	            os.setDeliveryDate(LocalDate.now().plusDays(4).toString());
	            os.setOrderQuantity(ct.getQuantity());
	            os.setOrderStatus(Status.PAYMENT_PENDING.toString());
	            os.setUserId((Long)(session.getAttribute("userId")));
	            os.setProductId(ct.getProductId());
	            os.setTotalPrice(ct.getTotal());
	            totalAmount += ct.getTotal();
	            User ob = rt.getForObject("http://localhost:8081/getbyid/"+os.getUserId(), User.class);
	            os.setShippingAddress(ob.getAddress()+" , "+ob.getCity()+" , "+ob.getPincode());
	            
	            // Integrate with Razorpay gateway
	            rt.postForObject("http://localhost:8082/CreateOrder", os, String.class);
	            rt.delete("http://localhost:8081/removefromcart/"+ct.getCartId());
	        }
	    }
	    
	    
	    JSONObject orderReq=new JSONObject();
	    orderReq.put("amount", totalAmount*100);
	    orderReq.put("currency", "INR");
	    User user = rt.getForObject("http://localhost:8081/getbyid/"+(Long)(session.getAttribute("userId")), User.class);
	    orderReq.put("receipt", user.getUsername());
	    
	    RazorpayClient razorpay = new RazorpayClient(razorPayKey, razorPaySecret);
	    Order razorpayOrder = razorpay.orders.create(orderReq);
	    String razorPayOrderId = razorpayOrder.get("id"); 

	    model.addAttribute("razorpayKey", razorPayKey);
	    model.addAttribute("razorpayOrderId", razorPayOrderId); 
	    model.addAttribute("amount", razorpayOrder.get("amount"));
	    model.addAttribute("user", user);
	    
	
	    OrderSe existingOrder[] = rt.getForObject("http://localhost:8082/getorderbyuid/" + (Long)(session.getAttribute("userId")), OrderSe[].class);

	    for(OrderSe order:existingOrder)
	    {
	    	order.setRazorpayOrderId(razorpayOrder.get("id"));
	    	 
		    rt.put("http://localhost:8082/updateOrder", order);
	    }
	    

	   
	    
	    model.addAttribute("successMessage", "Order placed successfully!");
	  //  System.out.println("razorpayOrder id is : "+razorPayOrderId);
	    return "razorpayCheckout";
	}
	
	
	@GetMapping("/getorder")
	public String getOrder(Model model)
	{
		if(session==null)
		{
			return "redirect:/login";
		}
		else
		{
			OrderSe[] ob = rt.getForObject("http://localhost:8082/getorderbyuid/"+(Long)(session.getAttribute("userId")), OrderSe[].class);
			for(OrderSe x:ob)
			{
//				System.out.println(x.getProductId());
//				System.out.println("----");
				x.setProductName(rt.getForEntity("http://localhost:8082/getname/"+x.getProductId(), String.class).getBody());
				x.setProductImage(rt.getForEntity("http://localhost:8082/getimage/"+x.getProductId(), String.class).getBody());
			} 
			model.addAttribute("order", ob);
			model.addAttribute("UserName", (String)(session.getAttribute("userName")));
			
			
			return "orders";
		}
	}
	
	@PostMapping("/updatestatus/{orderId}")
	public String updateStatus(@PathVariable Long orderId)
	{
		rt.put("http://localhost:8082/updatestatus/"+orderId, orderId);
		return "redirect:/getorder";
	}
	
	@GetMapping("/profile")
	public String profile(Model model)
	{
		if(session==null)
		{
			return "redirect:/login";
		}
		else
		{
			User ob = rt.getForObject("http://localhost:8081/getbyid/"+(Long)(session.getAttribute("userId")), User.class);
			model.addAttribute("user", ob);
			return "profile";
		}
	}
	
	@GetMapping("/logout")
	public String logout(HttpServletRequest req)
	{
		
		if(session==null)
		{
			return "redirect:/login";
		}
		else
		{
			req.getSession().invalidate();
			session=null;
			return "redirect:login";
		}
	}
	
	
}
