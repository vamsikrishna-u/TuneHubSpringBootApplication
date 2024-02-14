package com.example.demo.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.Entities.Songs;
import com.example.demo.Services.SongServices;
@Controller
public class SongController {
	
	
	@Autowired
	SongServices songServ;
	
	@PostMapping("/map-addsongsin")
	public String letsAdd(@ModelAttribute Songs song) {
		
		if(songServ.SongExist(song.getName())==false) {
		songServ.addSong(song);
		return "songsuccess";
		}
		else
		{
			return "songFail";
		}
		
	}
	
	@GetMapping("/map-showallsongs")
	public String showAllSongs(Model model)
	{
		List<Songs>songList=songServ.fetchSongs();
		model.addAttribute("songsList",songList);
		return "allsongs";
	}
	
	/*@GetMapping("/showallcustomersongs")
	public String showAllCustomerSongs(Model model)
	{
		
		boolean primeStatus=true;
		if(primeStatus==true)
		{
			List<Songs>songList=songServ.fetchSongs();
			model.addAttribute("songsList",songList);
			return "allsongs";
		}
		else
		{
			return "dopayment";
		}
		
	}*/
	
	

}
