package com.example.nagoyameshi.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.nagoyameshi.entity.EmailVerificationToken;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.SignupForm;
import com.example.nagoyameshi.repository.EmailVerificationTokenRepository;
import com.example.nagoyameshi.repository.UserRepository;

import jakarta.validation.Valid;

@Controller
public class SignupController {
	private final EmailVerificationTokenRepository emailVerificationTokenRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public SignupController(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			EmailVerificationTokenRepository emailVerificationTokenRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.emailVerificationTokenRepository = emailVerificationTokenRepository;
	}

	@GetMapping("/signup")
	public String signup(Model model) {

		model.addAttribute("signupForm", new SignupForm());

		return "user/signup";
	}

	@PostMapping("/signup")
	public String signup(

			@Valid SignupForm signupForm,
			BindingResult bindingResult,
			Model model) {

		if (bindingResult.hasErrors()) {
			return "user/signup";
		}

		if (userRepository.existsByEmail(signupForm.getEmail())) {
			bindingResult.rejectValue(
					"email",
					"emailExists",
					"このメールアドレスは既に登録されています。");
			return "user/signup";
		}

		User user = new User();
		user.setName(signupForm.getName());
		user.setEmail(signupForm.getEmail());
		user.setPassword(passwordEncoder.encode(signupForm.getPassword()));
		user.setRole("ROLE_USER");
		user.setEnabled(false);
		userRepository.save(user);

		String token = UUID.randomUUID().toString();

		EmailVerificationToken verificationToken = new EmailVerificationToken();
		verificationToken.setUser(user);
		verificationToken.setToken(token);
		verificationToken.setExpiresAt(LocalDateTime.now().plusHours(24));

		emailVerificationTokenRepository.save(verificationToken);

		model.addAttribute("verificationUrl",
				"http://localhost:8080/signup/verify?token=" + token);

		return "user/signup-sent";
	}

	@GetMapping("/signup/verify")
	public String verify(@RequestParam String token, Model model) {

		EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token).orElse(null);

		if (verificationToken == null ||
				verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {

			model.addAttribute("errorMessage", "認証リンクが無効、または期限切れです。");
			return "user/signup-sent";
		}

		User user = verificationToken.getUser();
		user.setEnabled(true);
		userRepository.save(user);

		emailVerificationTokenRepository.delete(verificationToken);

		return "redirect:/login?emailVerified";
	}
}