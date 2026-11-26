package com.example.demo.Controller;


import java.util.List;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.Entities.Playlist;
import com.example.demo.Entities.Songs;
import com.example.demo.Entities.Users;
import com.example.demo.Services.PlayListServices;
import com.example.demo.Services.SongServices;
import com.example.demo.Services.UserServices;


@Controller
public class UserController {
	
	private final UserServices userServ;
	private final SongServices songServ;
	private final PlayListServices pServ;
	private final String razorpayKeyId;

	public UserController(UserServices userServ, SongServices songServ, PlayListServices pServ,
			@Value("${razorpay.key-id}") String razorpayKeyId) {
		this.userServ = userServ;
		this.songServ = songServ;
		this.pServ = pServ;
		this.razorpayKeyId = razorpayKeyId;
	}
	
	
	
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
	
	@GetMapping("/showallsongs")
	public String customerSongs(Principal principal, Model model)
	{
		Users user=userServ.getUser(principal.getName());
		boolean status=user.isPremium();
		if(status==true)
		{
			List<Songs>songs=songServ.fetchSongs();
			model.addAttribute("songs", songs);
			return "customerviewsongs";
		}
		else
		{
			model.addAttribute("razorpayKeyId", razorpayKeyId);
			return "samplepayment";
		}		
	}
	@GetMapping("/change-password")
	public String changePasswordForm()
	{
		return "emailfoundresetpass";
	}
	
	@PostMapping("/passwordresetformaction")
	public String passwordUpdating(Principal principal, @RequestParam String currentPassword,
			@RequestParam String pass1,
			@RequestParam String passreset, Model model)
	{
		if (!pass1.equals(passreset)) {
			model.addAttribute("passwordMismatch", true);
			return "emailfoundresetpass";
		}
		if (!userServ.updatePassword(principal.getName(), currentPassword, pass1)) {
			model.addAttribute("currentPasswordInvalid", true);
			return "emailfoundresetpass";
		}
		return "redirect:/map-login?passwordReset";
	}	
	@GetMapping("/usercreateplaylists")
	public String userCreatePlaylists(Model model)
	{
		List<Songs>songs=songServ.fetchSongs();
		model.addAttribute("songs", songs);
		return "usercreateplaylists";
	}
	
	@PostMapping("/adduserplaylists")
	public String playListDisplay(Principal principal,@ModelAttribute Playlist plist)
	{
		Users user=userServ.getUser(principal.getName());
		 user.getUserplaylist().add(plist);
		userServ.addPlaylistToUser(user, plist);
		for(Songs song:plist.getSongs() )
		{
			song.getPlaylist().add(plist);
			songServ.updateSong(song);
		}
		return "userplaylistsuccess";
	}
	
	@GetMapping("viewcustomerplaylist")
	public String fetchLists(Model model, Principal principal)
	{
		List<Playlist> playlist=pServ.userPlayList(principal.getName());
		model.addAttribute("playlist", playlist);
		return "userviewplaylist";
	}
	
	//Admin Adding Playlist
	@PostMapping("/add")
	public String adminPlayListDisplay(Principal principal,@ModelAttribute Playlist plist)
	{
		Users user=userServ.getUser(principal.getName());
		 user.getUserplaylist().add(plist);
		userServ.addPlaylistToUser(user, plist);
		for(Songs song:plist.getSongs() )
		{
			song.getPlaylist().add(plist);
			songServ.updateSong(song);
		}
		return "Playlistsuccess";
	}
	
	@GetMapping("/map-viewadminplaylist")
	public String adminfetchLists(Model model, Principal principal)
	{
		List<Playlist> playlist=pServ.userPlayList(principal.getName());
		model.addAttribute("playlist", playlist);
		return "adminviewplaylist";
	}
	
}
	
	
	
	
	
	
	


