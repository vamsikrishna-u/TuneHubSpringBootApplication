package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NavController {
	private final String razorpayKeyId;

	public NavController(@Value("${razorpay.key-id}") String razorpayKeyId) {
		this.razorpayKeyId = razorpayKeyId;
	}

	@GetMapping("/")
	public String index() {
		return "index";
	}

	@GetMapping("/home")
	public String home(Authentication authentication) {
		return authentication.getAuthorities().stream()
				.anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))
				? "redirect:/adminhome" : "redirect:/customerhome";
	}

	@GetMapping("/adminhome")
	public String adminHome() {
		return "adminhome";
	}

	@GetMapping("/customerhome")
	public String customerHome() {
		return "customerhome";
	}

	@GetMapping("/access-denied")
	public String accessDenied() {
		return "access-denied";
	}
	
	@GetMapping("/map-register")
	public String plsRegister()
	{
		return "register";
	}
	
	@GetMapping("/map-login")
	public String plsLogin()
	{
		return "login";
	}
	@GetMapping("/loginfail")
	public String plsLoginAgain()
	{
		return "loginfail";
	}
	
	@GetMapping("/clicktoaddsong")
	public String addSongs(Model model)
	{
		
		return "addsongs";
		
	}
	
	@GetMapping("/sample")
	public String samplePayment(Model model)
	{
		model.addAttribute("razorpayKeyId", razorpayKeyId);
		return "samplepayment";
		
	}
	@GetMapping("/forgotpassword")
	public String forgetPassword()
	{
		return "forgotpassword";
	}
	
	
	
	
	

	
	
	


}
