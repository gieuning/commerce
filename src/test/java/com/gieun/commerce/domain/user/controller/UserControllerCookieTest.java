package com.gieun.commerce.domain.user.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gieun.commerce.domain.cart.service.CartService;
import com.gieun.commerce.domain.user.dto.response.TokenResponse;
import com.gieun.commerce.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerCookieTest {

  @Autowired
  MockMvc mockMvc;

  @MockitoBean
  UserService userService;

  @MockitoBean
  CartService cartService;

  @Test
  void loginIssuesHttpOnlyCookieAndNoTokenInBody() throws Exception {
    when(userService.login(any())).thenReturn(TokenResponse.of("issued-token", 1L));

    mockMvc.perform(post("/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"user@example.com\",\"password\":\"password\"}"))
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("access_token=issued-token")))
        .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
        .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Lax")))
        // 토큰은 바디에 노출되지 않아야 한다
        .andExpect(content().string(not(containsString("issued-token"))));
  }

  @Test
  void logoutExpiresCookie() throws Exception {
    mockMvc.perform(post("/users/logout"))
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("access_token=")))
        .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
  }
}
