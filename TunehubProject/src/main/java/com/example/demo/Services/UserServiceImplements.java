package com.example.demo.Services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.Entities.Playlist;
import com.example.demo.Entities.Users;
import com.example.demo.Repositories.UserRepositories;

@Service
public class UserServiceImplements implements UserServices {

	private final UserRepositories userRepo;
	private final PasswordEncoder passwordEncoder;

	public UserServiceImplements(UserRepositories userRepo, PasswordEncoder passwordEncoder) {
		this.userRepo = userRepo;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public boolean byEmail(String emailId) {
		Users user = userRepo.findByEmailId(emailId);

		if (user == null) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public String addUser(Users userServ) {
		userServ.setId(0);
		userServ.setPassword(passwordEncoder.encode(userServ.getPassword()));
		userServ.setRole("CUSTOMER");
		userServ.setPremium(false);
		userServ.setUserplaylist(new ArrayList<>());
		userRepo.save(userServ);
		return "User created and added";
	}

	@Override
	public boolean validateUser(String email, String password) {
		// TODO Auto-generated method stub
		Users user = userRepo.findByEmailId(email);
		return user != null && passwordEncoder.matches(password, user.getPassword());

	}

	@Override
	public String validateRole(String email) {
		// TODO Auto-generated method stub
		Users user = userRepo.findByEmailId(email);
		String roleInfo = user.getRole();

		return roleInfo;
	}

	@Override
	public List<Users> fetchUsers() {
		// TODO Auto-generated method stub
		List<Users> userList = userRepo.findAll();
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
	public boolean updatePassword(String email, String currentPassword, String newPassword) {
		Users user = userRepo.findByEmailId(email);
		if (user == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
			return false;
		}
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepo.save(user);
		return true;
	}

	@Override
	public void addPlaylistToUser(Users user, Playlist plist) {
		user.getUserplaylist().add(plist);
		userRepo.save(user);

	}

}
