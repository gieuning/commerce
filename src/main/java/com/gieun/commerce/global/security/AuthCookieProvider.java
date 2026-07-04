package com.gieun.commerce.global.security;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Access Token을 담는 HttpOnly 쿠키를 생성/삭제한다. 쿠키 속성을 한 곳에서 관리한다.
 * HttpOnly는 JS의 토큰 읽기를 차단해 XSS를 통한 토큰 탈취를 방지한다(주입 스크립트가 쿠키를 타고 요청하는 것까지 막지는 못함).
 * SameSite=Lax는 크로스사이트 요청의 쿠키 자동 전송을 완화한다(top-level GET 등 일부는 여전히 전송 — 완전 차단 아님).
 */
@Component
public class AuthCookieProvider {

  public static final String ACCESS_TOKEN_COOKIE = "access_token";
  private static final String SAME_SITE = "Lax";
  private static final String PATH = "/";

  private final long expirationMs;
  private final boolean secure; // 로컬 http는 false, 운영 https는 true

  public AuthCookieProvider(@Value("${jwt.expiration}") long expirationMs,
      @Value("${jwt.cookie.secure:false}") boolean secure) {
    this.expirationMs = expirationMs;
    this.secure = secure;
  }

  public ResponseCookie create(String token) {
    return baseCookie(token, Duration.ofMillis(expirationMs));
  }

  public ResponseCookie clear() {
    return baseCookie("", Duration.ZERO);
  }

  private ResponseCookie baseCookie(String value, Duration maxAge) {
    return ResponseCookie.from(ACCESS_TOKEN_COOKIE, value)
        .httpOnly(true)
        .secure(secure)
        .sameSite(SAME_SITE)
        .path(PATH)
        .maxAge(maxAge)
        .build();
  }
}
