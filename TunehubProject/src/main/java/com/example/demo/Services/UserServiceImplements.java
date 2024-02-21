package com.example.demo.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Entities.Playlist;
import com.example.demo.Entities.Songs;
import com.example.demo.Entities.Users;
import com.example.demo.Repositories.SongRepositories;
import com.example.demo.Repositories.UserRepositories;

@Service
public class UserServiceImplements implements UserServices{

		@Autowired
		UserRepositories userRepo;
	
		@Override
		public boolean byEmail(String emailId) {
			Users user=userRepo.findByEmailId(emailId);
			
			if(user==null) {
				return false;
			}
				else {
					return true;
			}
		}
		

		@Override
		public String addUser(Users userServ) {
			userRepo.save(userServ);
			return "User created and added";
		}
		
		@Override
		public boolean validateUser(String email,String password) {
			// TODO Auto-generated method stub
			Users user=userRepo.findByEmailId(email);
			String dbPassword=user.getPassword();
			
			if(dbPassword.equals(password))
			{
				
				return true;
			}
			else
			{
				return false;
			}
			
		}

		@Override
		public String validateRole(String email) {
			// TODO Auto-generated method stub
			Users user=userRepo.findByEmailId(email);
			String roleInfo=user.getRole();
			
			
			return roleInfo;
		}
		

		@Override
		public List<Users> fetchUsers() {
			// TODO Auto-generated method stub
			List<Users> userList=userRepo.findAll();
			return userList;
		}


		@Override
		public Users getUser(String emailId) {
			return userRepo.findByEmailId(emailId);
			
		}


		@Override
		public void updateUser(Users user) {
			// TODO Auto-generated method stub
			userRepo.save(user);
			
		}


		@Override
		public void addPlaylistToUser(Users user, Playlist plist) {
			user.getUserplaylist().add(plist);
	        userRepo.save(user);
			
		}

	}


