package com.example.demo.Services;

import java.util.List;

import com.example.demo.Entities.Playlist;
import com.example.demo.Entities.Songs;


public interface PlayListServices {
	
	public String addPlayList(Playlist plist);
	public List<Playlist> playlistfindAll();
	public List<Playlist> userPlayList(String email);
	//public List<Playlist>userPlayListings(int userId);
	
	

}
