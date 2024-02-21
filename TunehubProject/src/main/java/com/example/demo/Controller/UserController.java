package com.example.demo.Controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.Entities.Playlist;
import com.example.demo.Entities.Songs;
import com.example.demo.Entities.Users;
import com.example.demo.Repositories.UserRepositories;
import com.example.demo.Services.PlayListServices;
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
	@Autowired
	PlayListServices pServ;
	
	
	
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
	    
	    
	    if(userServ.byEmail(email)==false)
	    {
	    	return "loginfail";
	    }
	    else if (userServ.validateUser(email, password)) {
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
	@PostMapping("/resetpassword")
	public String resetPassword(HttpSession session,@RequestParam String email)
	{
		boolean exist=userServ.byEmail(email);
		if(exist==true)
		{
			session.setAttribute("email", email);
			return "emailfoundresetpass";
		}
		else {
			return "forgotpassword";
		}	
	}
	
	@PostMapping("/passwordresetformaction")
	public String passwordUpdating(HttpSession session,@RequestParam String pass1)
	{
		String strEmail=(String)session.getAttribute("email");
		Users user=userServ.getUser(strEmail);
		user.setPassword(pass1);
		userServ.updateUser(user);
		System.out.println("Updated password : "+pass1);
		return "login";
	}	
	@GetMapping("/usercreateplaylists")
	public String userCreatePlaylists(Model model)
	{
		List<Songs>songs=songServ.fetchSongs();
		model.addAttribute("songs", songs);
		return "usercreateplaylists";
	}
	
	@PostMapping("/adduserplaylists")
	public String playListDisplay(HttpSession session,@ModelAttribute Playlist plist)
	{
		String email=(String)session.getAttribute("email");
		Users user=userServ.getUser(email);
		 user.getUserplaylist().add(plist);
		userServ.addPlaylistToUser(user, plist);
		//userServ.updateUser(user);
		
		
		for(Songs song:plist.getSongs() )
		{
			song.getPlaylist().add(plist);
			songServ.updateSong(song);
		}
		return "userplaylistsuccess";
	}
	
	@GetMapping("viewcustomerplaylist")
	public String fetchLists(Model model,HttpSession session)
	{
		String email=(String)session.getAttribute("email");
		
		List<Playlist> playlist=pServ.userPlayList(email);
		model.addAttribute("playlist", playlist);
		return "userviewplaylist";
	}
}
	
	
	
	
	
	
	


