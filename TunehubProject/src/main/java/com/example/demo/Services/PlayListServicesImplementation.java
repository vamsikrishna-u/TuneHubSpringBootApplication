package com.example.demo.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Entities.Playlist;
import com.example.demo.Entities.Users;
import com.example.demo.Repositories.PlayListRepositories;
import com.example.demo.Repositories.UserRepositories;

@Service
public class PlayListServicesImplementation implements PlayListServices {
	
	@Autowired
	PlayListRepositories pRepo;

	@Override
	public String addPlayList(Playlist plist) {
		pRepo.save(plist);
		return "play list added";
	}
	@Override
	public List<Playlist> playlistfindAll() {
		return pRepo.findAll();	
	}
	
	@Autowired
	UserRepositories userRepo;
	@Override
	public List<Playlist> userPlayList(String email) {
		Users user=userRepo.findByEmailId(email);
		List<Playlist>ll=user.getUserplaylist();
		return ll;
	}
}
