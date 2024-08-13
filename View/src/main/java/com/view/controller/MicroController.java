package com.view.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

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
	
	@PostMapping("/addtocart/{productId}/{quantity}/{price}")
	public String addToCart(@PathVariable Long productId,@PathVariable Integer quantity,@PathVariable Double price)
	{
		CartEx cartex=new CartEx();
		cartex.setProductId(productId);
		cartex.setQuantity(quantity);
		cartex.setUserId((Long) session.getAttribute("userId"));
		cartex.setPrice(price);
		cartex.setTotal(price);
		cartex.setImagePath(rt.getForEntity("http://localhost:8082/getimage/"+productId, String.class).getBody());
		cartex.setProductName(rt.getForEntity("http://localhost:8082/getname/"+productId, String.class).getBody());
		cartex.setProductDesc(rt.getForEntity("http://localhost:8082/getdesc/"+productId, String.class).getBody());
		rt.postForEntity("http://localhost:8081/addtocart", cartex, String.class);
		return "redirect:/getallproducts";
		
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
	    model.addAttribute("UserName", userId.toString()); // Convert Long to String

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
	
	@PostMapping("/createorder")
	public String createOrder()
	{
		CartEx[] forObject = rt.getForObject("http://localhost:8081/getcartbyuid/"+(Long)(session.getAttribute("userId")), CartEx[].class);
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
				os.setOrderStatus(Status.ORDER_CONFIRMED.toString());
				os.setUserId((Long)(session.getAttribute("userId")));
				os.setProductId(ct.getProductId());
				os.setTotalPrice(ct.getTotal());
				User ob = rt.getForObject("http://localhost:8081/getbyid/"+os.getUserId(), User.class);
				os.setShippingAddress(ob.getAddress()+" , "+ob.getCity()+" , "+ob.getPincode());
				rt.postForObject("http://localhost:8082/CreateOrder", os, String.class);	
				rt.delete("http://localhost:8081/removefromcart/"+ct.getCartId());
			}
		}
		return "redirect:/getcart";
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
