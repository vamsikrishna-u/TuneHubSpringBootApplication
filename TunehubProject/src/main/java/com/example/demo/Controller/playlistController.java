package com.example.demo.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.Entities.Playlist;
import com.example.demo.Entities.Songs;
import com.example.demo.Services.PlayListServices;
import com.example.demo.Services.SongServices;

import jakarta.servlet.http.HttpSession;

@Controller
public class playlistController {
	
	@Autowired
	PlayListServices pServ;
	@Autowired
	SongServices songServ;

	
	
	@GetMapping("/map-createplaylist")
	public String CreatePlayList(Model model)
	{
		
		List<Songs>songslist=songServ.fetchSongs();
		
		model.addAttribute("songs",songslist);
		return "createplaylist";
	}
	
	@GetMapping("/map-viewplaylist")
	public String fetchLists(Model model)
	{
		List<Playlist> playlist=pServ.playlistfindAll();
		model.addAttribute("playlist", playlist);
		return "viewplaylist";
	}
	
	@GetMapping("/map-userCreateplaylist")
	public String userCreatePlayList(Model model)
	{
		
		List<Songs>songslist=songServ.fetchSongs();
		
		model.addAttribute("songs",songslist);
		return "createplaylist";
	}

}
