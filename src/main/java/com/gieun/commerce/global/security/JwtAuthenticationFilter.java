package com.gieun.commerce.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import com.gieun.commerce.domain.user.entity.Role;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String token = resolveToken(request);

    if (token != null && jwtTokenProvider.validateToken(token)) {
      Long userId = jwtTokenProvider.getUserId(token);
      String role = jwtTokenProvider.getRole(token);

      if (isValidRole(role)) {
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }

    filterChain.doFilter(request, response);
  }

  private boolean isValidRole(String role) {
    if (role == null) {
      return false;
    }
    try {
      Role.valueOf(role);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  private String resolveToken(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return null;
    }
    return Arrays.stream(request.getCookies())
        .filter(cookie -> AuthCookieProvider.ACCESS_TOKEN_COOKIE.equals(cookie.getName()))
        .map(Cookie::getValue)
        .filter(StringUtils::hasText)
        .findFirst()
        .orElse(null);
  }
}
