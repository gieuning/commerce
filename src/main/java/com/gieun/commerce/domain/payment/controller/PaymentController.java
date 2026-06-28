package com.gieun.commerce.domain.payment.controller;

import com.gieun.commerce.domain.payment.dto.request.PaymentCancelRequest;
import com.gieun.commerce.domain.payment.dto.request.PaymentConfirmRequest;
import com.gieun.commerce.domain.payment.dto.request.PaymentCreateRequest;
import com.gieun.commerce.domain.payment.dto.response.PaymentDetailResponse;
import com.gieun.commerce.domain.payment.dto.response.PaymentResponse;
import com.gieun.commerce.domain.payment.service.PaymentService;
import com.gieun.commerce.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment", description = "결제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
@SecurityRequirement(name = "JWT")
public class PaymentController {

  private final PaymentService paymentService;

  @Operation(summary = "결제 요청 생성")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<PaymentResponse> request(
      @AuthenticationPrincipal Long userId,
      @Valid @RequestBody PaymentCreateRequest request
  ) {
    return ApiResponse.ok(paymentService.request(userId, request));
  }

  @Operation(summary = "내 결제 상세 조회")
  @GetMapping("/{paymentId}")
  public ApiResponse<PaymentResponse> getPayment(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long paymentId
  ) {
    return ApiResponse.ok(paymentService.getPayment(userId, paymentId));
  }

  @Operation(summary = "내 결제 상세 이력 조회")
  @GetMapping("/{paymentId}/details")
  public ApiResponse<PaymentDetailResponse> getPaymentDetail(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long paymentId
  ) {
    return ApiResponse.ok(paymentService.getPaymentDetail(userId, paymentId));
  }

  @Operation(summary = "결제 승인")
  @PostMapping("/confirm")
  public ApiResponse<PaymentResponse> confirm(
      @AuthenticationPrincipal Long userId,
      @Valid @RequestBody PaymentConfirmRequest request
  ) {
    return ApiResponse.ok(paymentService.confirm(userId, request));
  }

  @Operation(summary = "결제 취소")
  @PostMapping("/{paymentId}/cancel")
  public ApiResponse<PaymentResponse> cancel(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long paymentId,
      @Valid @RequestBody PaymentCancelRequest request
  ) {
    return ApiResponse.ok(paymentService.cancel(userId, paymentId, request));
  }
}
