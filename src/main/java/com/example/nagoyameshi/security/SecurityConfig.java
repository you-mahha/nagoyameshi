package com.example.nagoyameshi.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http
	    .csrf(csrf -> csrf
	        .ignoringRequestMatchers("/payment/webhook")
	    )
	    .authorizeHttpRequests(auth -> auth
	    	.requestMatchers("/payment/webhook").permitAll() // ★これを先に書く	
	        .requestMatchers(
	            "/",
	            "/login",
	            "/signup",
	            "/signup/**",
	            "/shops/**",
	            "/css/**",
	            "/js/**",
	            "/images/**",
	            "/password-reset/**" // パスワード再設定 //
	        ).permitAll()
	        .requestMatchers("/admin/**").hasRole("ADMIN")
	        .anyRequest().authenticated()
	    )
	    .formLogin(login -> login
	        .loginPage("/login")
	        .loginProcessingUrl("/login")
	        .successHandler(authenticationSuccessHandler())
	        .permitAll()
	    )
	    .logout(logout -> logout
	        .logoutSuccessUrl("/")
	        .permitAll()
	    );
		
		return http.build();
		
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// ★ 追加
	@Bean
	public AuthenticationSuccessHandler authenticationSuccessHandler() {
		return (request, response, authentication) -> {
			  HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
		        SavedRequest savedRequest = requestCache.getRequest(request, response);

		        if (savedRequest != null) {
		            response.sendRedirect(savedRequest.getRedirectUrl());
		            return;
		        }

		        String redirectUrl = request.getParameter("redirectUrl");
				
				if(redirectUrl != null && !redirectUrl.isBlank()) {
					response.sendRedirect(redirectUrl);
					return;
				}
		        
		        boolean isAdmin = authentication.getAuthorities().stream()
		                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

		        if (isAdmin) {
		            response.sendRedirect("/admin");
		        } else {
		            response.sendRedirect("/");
		        }
		};
		
		
		}
	}
