package com.example.demo.Controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.Entities.Users;
import com.example.demo.Services.UserServices;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;

import jakarta.servlet.http.HttpSession;

@Controller
@ResponseBody
public class PaymentController {
	
	@Autowired
	UserServices userv;
	
	
	@PostMapping("/createOrder")
	public String createOrder()
	{
		RazorpayClient razorpay;
		Order order=null;
		
		try {
			razorpay = new RazorpayClient("rzp_test_OglT6jM5iA81Sy", "g4gEfkicF8FiGiZwx8T8t81M");
			
			JSONObject orderRequest = new JSONObject();
			orderRequest.put("amount",50000);
			orderRequest.put("currency","INR");
			orderRequest.put("receipt", "receipt#1");
			JSONObject notes = new JSONObject();
			notes.put("notes_key_1","Tea, Earl Grey, Hot");
			orderRequest.put("notes",notes);

			order = razorpay.orders.create(orderRequest);
			
		} catch (RazorpayException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return order.toString();

		
	}
	@PostMapping("/verify")
	@ResponseBody
	public boolean verifyPayment(@RequestParam  String orderId, @RequestParam String paymentId, @RequestParam String signature) {
	    try {
	        // Initialize Razorpay client with your API key and secret
	        RazorpayClient razorpayClient = new RazorpayClient("rzp_test_OglT6jM5iA81Sy", "g4gEfkicF8FiGiZwx8T8t81M");
	        // Create a signature verification data string
	        String verificationData = orderId + "|" + paymentId;

	        // Use Razorpay's utility function to verify the signature
	        boolean isValidSignature = Utils.verifySignature(verificationData, signature, "g4gEfkicF8FiGiZwx8T8t81M");

	        return isValidSignature;
	    } catch (RazorpayException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	
	//payment Success
	//Update premium uSer
	//Payment fail ->Redirected to payment
	
	@GetMapping("payment-success")
	public String paymentSuccess(HttpSession session )
	{
		String email=(String) session.getAttribute("email");
		Users user=userv.getUser(email);
		user.setPremium(true);
		userv.updateUser(user);
		
		
		return "login";
		
	}
	@GetMapping("payment-failure")
	public String paymentFail(String email)
	{
		Users user=userv.getUser(email);
		user.setPremium(false);
		userv.updateUser(user);
		return "sample";
	}

}
