package com.gieun.commerce.global.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.gieun.commerce.global.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void mapsIllegalArgumentExceptionToBadRequest() {
    // 엔티티/도메인 규칙 위반(음수 금액·수량 등)은 500이 아니라 400이어야 한다
    ResponseEntity<ApiResponse<Void>> response =
        handler.handleIllegalArgumentException(new IllegalArgumentException("취소 금액은 0보다 커야 합니다."));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void domainExceptionKeepsItsOwnStatus() {
    ResponseEntity<ApiResponse<Void>> response =
        handler.handleDomainException(new DomainException(DomainExceptionCode.NOT_FOUND_PAYMENT));

    assertThat(response.getStatusCode()).isEqualTo(DomainExceptionCode.NOT_FOUND_PAYMENT.getStatus());
  }
}
