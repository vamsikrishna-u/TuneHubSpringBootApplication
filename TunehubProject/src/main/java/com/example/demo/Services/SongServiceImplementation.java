package com.example.demo.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Entities.Songs;
import com.example.demo.Repositories.SongRepositories;

@Service
public class SongServiceImplementation implements SongServices {
	
	@Autowired
	SongRepositories songRepo;
	
	@Override
	public String addSong(Songs song) {
		songRepo.save(song);
		return "song saved and added";
	}
	@Override
	public List<Songs> fetchSongs() {
		
		List<Songs> userList=songRepo.findAll();
		return userList;
	}
	@Override
	public boolean SongExist(String name) {
		
		Songs song=songRepo.findByName(name);
		
		
		if(song==null) 
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	@Override
	public void updateSong(Songs song) {
		songRepo.save(song);
		
	}
		
}


