package com.example.demo.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers(HttpMethod.GET, "/", "/map-register", "/map-login", "/loginfail",
						"/forgotpassword", "/CSS/**", "/webjars/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/registerformcontrol").permitAll()
				.requestMatchers("/access-denied").permitAll()
				.requestMatchers("/change-password", "/passwordresetformaction").authenticated()
				.requestMatchers("/adminhome", "/clicktoaddsong", "/map-addsongsin", "/map-showallsongs",
						"/map-createplaylist", "/map-viewplaylist", "/add", "/map-viewadminplaylist")
					.hasRole("ADMIN")
				.requestMatchers("/customerhome", "/showallsongs", "/usercreateplaylists",
						"/map-userCreateplaylist", "/adduserplaylists", "/viewcustomerplaylist",
						"/sample", "/createOrder", "/verify", "/payment-success", "/payment-failure")
					.hasRole("CUSTOMER")
				.requestMatchers("/home").authenticated()
				.requestMatchers("/logout").authenticated()
				.anyRequest().denyAll())
			.formLogin(form -> form
				.loginPage("/map-login")
				.loginProcessingUrl("/loginformcontrol")
				.usernameParameter("email")
				.passwordParameter("password")
				.failureUrl("/loginfail")
				.successHandler((request, response, authentication) -> {
					String destination = authentication.getAuthorities().stream()
						.filter(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))
						.findAny()
						.map(authority -> "/adminhome")
						.orElse("/customerhome");
					response.sendRedirect(destination);
				})
				.permitAll())
			.logout(logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/map-login?logout")
				.invalidateHttpSession(true)
				.clearAuthentication(true)
				.deleteCookies("JSESSIONID"))
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
				.sessionFixation(fixation -> fixation.migrateSession()))
			.exceptionHandling(exception -> exception.accessDeniedPage("/access-denied"));

		return http.build();
	}
}
