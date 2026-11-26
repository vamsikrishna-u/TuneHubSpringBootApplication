package com.example.demo.Security;

import java.util.Locale;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.Entities.Users;
import com.example.demo.Repositories.UserRepositories;

@Service
public class TuneHubUserDetailsService implements UserDetailsService {

	private final UserRepositories userRepositories;

	public TuneHubUserDetailsService(UserRepositories userRepositories) {
		this.userRepositories = userRepositories;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Users account = userRepositories.findByEmailId(email);
		if (account == null) {
			throw new UsernameNotFoundException("User not found");
		}

		String role = account.getRole() == null ? "" : account.getRole().trim().toUpperCase(Locale.ROOT);
		String authorityRole = switch (role) {
			case "ADMIN" -> "ADMIN";
			case "USER", "CUSTOMER" -> "CUSTOMER";
			default -> throw new UsernameNotFoundException("User role is not supported");
		};

		return User.withUsername(account.getEmailId())
			.password(account.getPassword())
			.roles(authorityRole)
			.build();
	}
}
