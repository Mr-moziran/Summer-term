package com.project.demo.config;

import com.project.demo.dto.ApiErrorResponse;
import com.project.demo.entity.User;
import com.project.demo.entity.UserStatus;
import com.project.demo.repository.UserRepository;
import com.project.demo.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;

	private final UserRepository userRepository;

	private final ObjectMapper objectMapper;

	public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository, ObjectMapper objectMapper) {
		this.jwtService = jwtService;
		this.userRepository = userRepository;
		this.objectMapper = objectMapper;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader("Authorization");
		if (header == null || header.isBlank()) {
			filterChain.doFilter(request, response);
			return;
		}
		if (!header.startsWith("Bearer ")) {
			writeUnauthorized(response, "认证令牌格式不正确");
			return;
		}
		try {
			JwtService.JwtClaims claims = jwtService.verify(header.substring(7));
			User user = userRepository.findById(claims.userId())
					.orElseThrow(() -> new IllegalArgumentException("用户不存在"));
			if (user.getStatus() != UserStatus.ACTIVE) {
				throw new IllegalArgumentException("用户已禁用");
			}
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
					user.getId(),
					null,
					List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
			SecurityContextHolder.getContext().setAuthentication(authentication);
			filterChain.doFilter(request, response);
		}
		catch (IllegalArgumentException exception) {
			SecurityContextHolder.clearContext();
			writeUnauthorized(response, "认证令牌无效");
		}
	}

	private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(response.getWriter(), new ApiErrorResponse(401, message, null));
	}
}
