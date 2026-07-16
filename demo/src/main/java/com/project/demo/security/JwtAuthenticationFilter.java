package com.project.demo.security;

import com.project.demo.domain.dto.response.ApiErrorResponse;
import com.project.demo.domain.model.User;
import com.project.demo.domain.enums.UserStatus;
import com.project.demo.repository.UserRepository;
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

/**
 * JWT 请求过滤器。
 *
 * <p>每个 HTTP 请求只执行一次：解析 Authorization Bearer Token，校验签名和用户状态，
 * 并把用户 id 与角色写入 Spring Security 上下文，供后续权限规则和 CurrentUserService 使用。</p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;

	private final UserRepository userRepository;

	private final ObjectMapper objectMapper;

	private final TokenBlacklist tokenBlacklist;

	public JwtAuthenticationFilter(
			JwtService jwtService,
			UserRepository userRepository,
			ObjectMapper objectMapper,
			TokenBlacklist tokenBlacklist) {
		this.jwtService = jwtService;
		this.userRepository = userRepository;
		this.objectMapper = objectMapper;
		this.tokenBlacklist = tokenBlacklist;
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
			String rawToken = header.substring(7);
			if (tokenBlacklist.contains(rawToken)) {
				throw new IllegalArgumentException("令牌已失效");
			}
			JwtService.JwtClaims claims = jwtService.verify(rawToken);
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
