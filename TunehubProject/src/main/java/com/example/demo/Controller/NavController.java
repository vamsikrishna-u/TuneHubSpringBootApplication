package com.example.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NavController {
	
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
		return "login";
	}
	
	@GetMapping("/clicktoaddsong")
	public String addSongs(Model model)
	{
		
		return "addsongs";
		
	}
	
	@GetMapping("/sample")
	public String samplePayment(Model model)
	{
		
		return "samplepayment";
		
	}
	@GetMapping("/forgotpassword")
	public String forgetPassword()
	{
		return "forgotpassword";
	}
	
	@GetMapping("/testAuthor")
	public String testAuthor()
	{
		return "testAuthor";
	}
	
	@GetMapping("/newUserTestGitpull")
	public String testPull()
	{
		return "newusertest";
	}
	
	
	
	
	


}
