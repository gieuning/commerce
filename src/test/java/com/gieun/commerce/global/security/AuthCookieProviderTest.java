package com.gieun.commerce.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

class AuthCookieProviderTest {

  private final AuthCookieProvider provider = new AuthCookieProvider(3_600_000L, false);

  @Test
  void createIssuesHttpOnlyLaxCookieWithToken() {
    ResponseCookie cookie = provider.create("token-value");

    assertThat(cookie.getName()).isEqualTo(AuthCookieProvider.ACCESS_TOKEN_COOKIE);
    assertThat(cookie.getValue()).isEqualTo("token-value");
    assertThat(cookie.isHttpOnly()).isTrue();
    assertThat(cookie.getSameSite()).isEqualTo("Lax");
    assertThat(cookie.getPath()).isEqualTo("/");
    assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofMillis(3_600_000L));
  }

  @Test
  void clearIssuesExpiredEmptyCookie() {
    ResponseCookie cookie = provider.clear();

    assertThat(cookie.getName()).isEqualTo(AuthCookieProvider.ACCESS_TOKEN_COOKIE);
    assertThat(cookie.getValue()).isEmpty();
    assertThat(cookie.getMaxAge()).isEqualTo(Duration.ZERO);
    assertThat(cookie.isHttpOnly()).isTrue();
  }
}
