package com.example.demo.Services;

import java.util.List;

import com.example.demo.Entities.Playlist;
import com.example.demo.Entities.Songs;
import com.example.demo.Entities.Users;

public interface UserServices  {
	
	
		public String addUser(Users userServ);
		public List<Users>fetchUsers();
		public boolean byEmail(String emailId);
		
		public boolean validateUser(String email,String password);
		public String validateRole(String email);
		public Users getUser(String emailId);
		public void updateUser(Users user);
		public void addPlaylistToUser(Users user, Playlist plist);
		
		
		
		
		
		
		
		
	}


