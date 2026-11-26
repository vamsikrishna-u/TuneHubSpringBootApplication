package com.example.demo.Services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.demo.Entities.Users;
import com.example.demo.Repositories.UserRepositories;

@ExtendWith(MockitoExtension.class)
class UserServiceImplementsTest {

	@Mock
	private UserRepositories userRepositories;

	private BCryptPasswordEncoder passwordEncoder;
	private UserServiceImplements userService;

	@BeforeEach
	void setUp() {
		passwordEncoder = new BCryptPasswordEncoder();
		userService = new UserServiceImplements(userRepositories, passwordEncoder);
	}

	@Test
	void registrationHashesPasswordAndForcesCustomerRole() {
		Users submitted = new Users();
		submitted.setEmailId("new@example.com");
		submitted.setPassword("plain-password");
		submitted.setRole("ADMIN");
		submitted.setPremium(true);
		submitted.setId(99);

		userService.addUser(submitted);

		ArgumentCaptor<Users> savedUser = ArgumentCaptor.forClass(Users.class);
		verify(userRepositories).save(savedUser.capture());
		assertThat(savedUser.getValue().getPassword()).isNotEqualTo("plain-password");
		assertThat(passwordEncoder.matches("plain-password", savedUser.getValue().getPassword())).isTrue();
		assertThat(savedUser.getValue().getRole()).isEqualTo("CUSTOMER");
		assertThat(savedUser.getValue().isPremium()).isFalse();
		assertThat(savedUser.getValue().getId()).isZero();
	}

	@Test
	void passwordValidationUsesPasswordEncoder() {
		Users account = new Users();
		account.setEmailId("member@example.com");
		account.setPassword(passwordEncoder.encode("correct-password"));
		when(userRepositories.findByEmailId("member@example.com")).thenReturn(account);

		assertThat(userService.validateUser("member@example.com", "correct-password")).isTrue();
		assertThat(userService.validateUser("member@example.com", "incorrect-password")).isFalse();
	}

	@Test
	void passwordChangeRequiresCurrentPasswordAndHashesNewPassword() {
		Users account = new Users();
		account.setEmailId("member@example.com");
		account.setPassword(passwordEncoder.encode("current-password"));
		when(userRepositories.findByEmailId("member@example.com")).thenReturn(account);

		assertThat(userService.updatePassword("member@example.com", "incorrect-password", "new-password"))
			.isFalse();
		assertThat(userService.updatePassword("member@example.com", "current-password", "new-password"))
			.isTrue();
		assertThat(passwordEncoder.matches("new-password", account.getPassword())).isTrue();
	}
}
