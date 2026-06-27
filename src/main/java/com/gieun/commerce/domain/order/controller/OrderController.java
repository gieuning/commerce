package com.gieun.commerce.domain.order.controller;

import com.gieun.commerce.domain.order.dto.request.OrderCreateRequest;
import com.gieun.commerce.domain.order.dto.request.OrderSearchRequest;
import com.gieun.commerce.domain.order.dto.response.OrderResponse;
import com.gieun.commerce.domain.order.service.OrderService;
import com.gieun.commerce.global.response.ApiResponse;
import com.gieun.commerce.global.response.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Order", description = "주문 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
@SecurityRequirement(name = "JWT")
public class OrderController {

  private final OrderService orderService;

  @Operation(summary = "주문 생성")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<OrderResponse> create(@AuthenticationPrincipal Long userId,
      @Valid @RequestBody OrderCreateRequest request) {
    return ApiResponse.ok(orderService.create(userId, request));
  }

  @Operation(summary = "내 주문 목록 조회")
  @GetMapping
  public ApiResponse<PageResult<OrderResponse>> getOrders(
      @AuthenticationPrincipal Long userId,
      @ParameterObject @ModelAttribute OrderSearchRequest request,
      @ParameterObject @PageableDefault(size = 10) Pageable pageable
  ) {
    return ApiResponse.ok(orderService.getOrders(userId, request, pageable));
  }

  @Operation(summary = "내 주문 상세 조회")
  @GetMapping("/{orderId}")
  public ApiResponse<OrderResponse> getOrder(@AuthenticationPrincipal Long userId,
      @PathVariable Long orderId) {
    return ApiResponse.ok(orderService.getOrder(userId, orderId));
  }

  @Operation(summary = "주문 취소")
  @PatchMapping("/{orderId}/cancel")
  public ApiResponse<OrderResponse> cancel(@AuthenticationPrincipal Long userId,
      @PathVariable Long orderId) {
    return ApiResponse.ok(orderService.cancel(userId, orderId));
  }
}
