package com.gieun.commerce.domain.payment.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gieun.commerce.domain.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 결제 API의 권한 경계 검증: USER만 접근 가능(CLAUDE.md), ADMIN/비인증은 차단.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerAuthorizationTest {

  @Autowired
  MockMvc mockMvc;

  @MockitoBean
  PaymentService paymentService;

  @Test
  @WithMockUser(roles = "USER")
  void userCanAccessPayment() throws Exception {
    mockMvc.perform(get("/payments/1"))
        .andExpect(status().is2xxSuccessful());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminIsForbiddenFromPayment() throws Exception {
    mockMvc.perform(get("/payments/1"))
        .andExpect(status().isForbidden());
  }

  @Test
  void anonymousIsUnauthorized() throws Exception {
    // 미인증은 500이 아니라 401 (AuthenticationEntryPoint)
    mockMvc.perform(get("/payments/1"))
        .andExpect(status().isUnauthorized());
  }
}
