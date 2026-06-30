package com.project.demo.config;

import com.project.demo.dto.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	private final ObjectMapper objectMapper;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.objectMapper = objectMapper;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/auth/**").permitAll()
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						.requestMatchers("/api/ai/**").hasAnyRole("AGENT", "ADMIN")
						.requestMatchers("/api/notifications/**").authenticated()
						.requestMatchers(HttpMethod.POST, "/api/tickets/*/assign").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PATCH, "/api/tickets/*/status").hasAnyRole("AGENT", "ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/tickets/*/rate").hasRole("USER")
						.requestMatchers(HttpMethod.POST, "/api/tickets/*/replies").hasAnyRole("AGENT", "ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/tickets/*/replies").authenticated()
						.requestMatchers("/api/tickets/**").authenticated()
						.anyRequest().authenticated())
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint((request, response, exception) -> writeError(response, 401, "请先登录"))
						.accessDeniedHandler((request, response, exception) -> writeError(response, 403, "权限不足")))
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	@Bean
	UserDetailsService userDetailsService() {
		return username -> {
			throw new UsernameNotFoundException(username);
		};
	}

	private void writeError(HttpServletResponse response, int code, String message) throws java.io.IOException {
		response.setStatus(code);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(response.getWriter(), new ApiErrorResponse(code, message, null));
	}
}
