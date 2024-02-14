package com.example.demo.Controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.Entities.Songs;
import com.example.demo.Entities.Users;
import com.example.demo.Repositories.UserRepositories;
import com.example.demo.Services.SongServices;
import com.example.demo.Services.UserServiceImplements;
import com.example.demo.Services.UserServices;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {
	
	@Autowired
	UserServices userServ;
	@Autowired
	SongServices songServ;
	
	@PostMapping("/registerformcontrol")
	public String addUser(@ModelAttribute Users user) 
	{
		if(userServ.byEmail(user.getEmailId())==false) 
		{
			userServ.addUser(user);
			return "success";
		}
		else 
		{
			return "registerfail";
		}
	}
	
	@PostMapping("/loginformcontrol")
	public String validateUser(@RequestParam String email, @RequestParam String password,HttpSession session) {
	    boolean validatingResult = userServ.validateUser(email, password);

	    if (validatingResult) {
	    	session.setAttribute("email", email);
	        if ("user".equals(userServ.validateRole(email))) {
	        	
	            return "customerhome";
	        } else {
	            return "adminhome";
	        }
	    } else {
	        return "loginfail";
	    }
	}
	@GetMapping("/showallsongs")
	public String customerSongs(HttpSession session,Model model)
	{
		String email=(String)session.getAttribute("email");
		Users user=userServ.getUser(email);
		boolean status=user.isPremium();
		if(status==true)
		{
			List<Songs>songs=songServ.fetchSongs();
			model.addAttribute("songs", songs);
			return "customerviewsongs";
		}
		else
		{
			return "samplepayment";
		}
		
		
	}
	        //Logout
			@GetMapping("/logout")
			public String logout(HttpSession session)
			{
				session.invalidate();
				return "login";
				
			}
	
}
	
	
	
	
	
	
	


