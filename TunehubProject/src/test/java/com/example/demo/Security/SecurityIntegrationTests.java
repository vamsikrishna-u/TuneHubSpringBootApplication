package com.example.demo.Security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockHttpSession;

import com.example.demo.Controller.NavController;
import com.example.demo.Controller.PaymentController;
import com.example.demo.Controller.SongController;
import com.example.demo.Controller.UserController;
import com.example.demo.Controller.playlistController;
import com.example.demo.Entities.Users;
import com.example.demo.Payments.PaymentProcessor;
import com.example.demo.Repositories.UserRepositories;
import com.example.demo.Services.PlayListServices;
import com.example.demo.Services.SongServices;
import com.example.demo.Services.UserServices;

@WebMvcTest(controllers = { NavController.class, PaymentController.class, SongController.class,
		UserController.class, playlistController.class }, properties = {
			"razorpay.key-id=test-key-id", "razorpay.key-secret=test-key-secret" })
@Import({ SecurityConfig.class, TuneHubUserDetailsService.class })
class SecurityIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@MockBean
	private UserRepositories userRepositories;

	@MockBean
	private UserServices userServices;

	@MockBean
	private SongServices songServices;

	@MockBean
	private PlayListServices playListServices;

	@MockBean
	private PaymentProcessor paymentService;

	private Users admin;
	private Users customer;

	@BeforeEach
	void setUp() {
		admin = account("admin@example.com", "admin", "ADMIN");
		customer = account("customer@example.com", "customer", "user");
		when(userRepositories.findByEmailId(admin.getEmailId())).thenReturn(admin);
		when(userRepositories.findByEmailId(customer.getEmailId())).thenReturn(customer);
	}

	@Test
	void validAdminCredentialsAuthenticateAndRedirectToAdminHome() throws Exception {
		MockHttpSession existingSession = new MockHttpSession();
		String previousSessionId = existingSession.getId();

		MvcResult result = mockMvc.perform(post("/loginformcontrol")
				.session(existingSession)
				.with(csrf())
				.param("email", admin.getEmailId())
				.param("password", "correct-password"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/adminhome"))
			.andReturn();

		assertThat(result.getRequest().getSession(false).getId()).isNotEqualTo(previousSessionId);
	}

	@Test
	void loginFormRendersCsrfToken() throws Exception {
		mockMvc.perform(get("/map-login"))
			.andExpect(status().isOk())
			.andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"_csrf\"")));
	}

	@Test
	void publicRegistrationEndpointRemainsAvailable() throws Exception {
		when(userServices.byEmail("new@example.com")).thenReturn(false);

		mockMvc.perform(post("/registerformcontrol")
				.with(csrf())
				.param("username", "New member")
				.param("emailId", "new@example.com")
				.param("password", "correct-password")
				.param("role", "ADMIN"))
			.andExpect(status().isOk())
			.andExpect(view().name("success"));

		verify(userServices).addUser(org.mockito.ArgumentMatchers.any(Users.class));
	}

	@Test
	void invalidPasswordDoesNotAuthenticate() throws Exception {
		mockMvc.perform(post("/loginformcontrol")
				.with(csrf())
				.param("email", admin.getEmailId())
				.param("password", "wrong-password"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/loginfail"));
	}

	@Test
	void legacyUserRoleAuthenticatesAsCustomer() throws Exception {
		mockMvc.perform(post("/loginformcontrol")
				.with(csrf())
				.param("email", customer.getEmailId())
				.param("password", "correct-password"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/customerhome"));
	}

	@Test
	void unauthenticatedAdminRequestRedirectsToLogin() throws Exception {
		mockMvc.perform(get("/map-showallsongs"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("http://localhost/map-login"));
	}

	@Test
	void stateChangingRequestRequiresCsrfToken() throws Exception {
		mockMvc.perform(post("/map-addsongsin").with(user("admin").roles("ADMIN")))
			.andExpect(status().isForbidden());
	}

	@Test
	void customerCannotAccessAdminEndpoint() throws Exception {
		mockMvc.perform(get("/map-showallsongs").with(user("customer").roles("CUSTOMER")))
			.andExpect(status().isForbidden())
			.andExpect(forwardedUrl("/access-denied"));
	}

	@Test
	void adminCanAccessAdminEndpoint() throws Exception {
		mockMvc.perform(get("/map-showallsongs").with(user("admin").roles("ADMIN")))
			.andExpect(status().isOk())
			.andExpect(view().name("allsongs"));
	}

	@Test
	void customerCanAccessCustomerPlaylistPage() throws Exception {
		mockMvc.perform(get("/usercreateplaylists").with(user("customer").roles("CUSTOMER")))
			.andExpect(status().isOk())
			.andExpect(view().name("usercreateplaylists"));
	}

	@Test
	void customerCanOpenOwnPasswordChangeForm() throws Exception {
		mockMvc.perform(get("/change-password").with(user("customer").roles("CUSTOMER")))
			.andExpect(status().isOk())
			.andExpect(view().name("emailfoundresetpass"));
	}

	@Test
	void unauthenticatedPaymentVerificationIsRejected() throws Exception {
		mockMvc.perform(post("/verify")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"orderId\":\"order_test\",\"paymentId\":\"pay_test\",\"signature\":\"signature\"}"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("http://localhost/map-login"));
	}

	@Test
	void directlyOpeningLegacyPaymentSuccessRouteCannotActivatePremium() throws Exception {
		mockMvc.perform(get("/payment-success").with(user("customer").roles("CUSTOMER")))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/showallsongs"));
		verifyNoInteractions(paymentService);
	}

	@Test
	void directPaymentFailureRouteDoesNotChangePremiumState() throws Exception {
		mockMvc.perform(get("/payment-failure").with(user("customer").roles("CUSTOMER")))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/showallsongs"));
		verifyNoInteractions(paymentService);
	}

	@Test
	void logoutInvalidatesSessionAndRedirectsToLogin() throws Exception {
		MockHttpSession session = new MockHttpSession();
		mockMvc.perform(post("/logout").session(session).with(user("admin").roles("ADMIN")).with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/map-login?logout"));
		assertThat(session.isInvalid()).isTrue();
	}

	private Users account(String email, String name, String role) {
		Users account = new Users();
		account.setEmailId(email);
		account.setUsername(name);
		account.setRole(role);
		account.setPassword(passwordEncoder.encode("correct-password"));
		return account;
	}
}
