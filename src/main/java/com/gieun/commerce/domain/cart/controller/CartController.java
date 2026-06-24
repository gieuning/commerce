package com.gieun.commerce.domain.cart.controller;

import com.gieun.commerce.domain.cart.dto.request.CartItemAddRequest;
import com.gieun.commerce.domain.cart.dto.request.CartItemOptionUpdateRequest;
import com.gieun.commerce.domain.cart.dto.request.CartItemUpdateRequest;
import com.gieun.commerce.domain.cart.dto.response.CartResponse;
import com.gieun.commerce.domain.cart.service.CartService;
import com.gieun.commerce.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cart", description = "장바구니 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
@SecurityRequirement(name = "JWT")
public class CartController {

  private final CartService cartService;

  @Operation(summary = "내 장바구니 조회")
  @GetMapping
  public ApiResponse<CartResponse> getCart(@AuthenticationPrincipal Long userId) {
    return ApiResponse.ok(cartService.getCart(userId));
  }

  @Operation(summary = "장바구니 상품 추가")
  @PostMapping("/items")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<CartResponse> addItem(@AuthenticationPrincipal Long userId,
      @Valid @RequestBody CartItemAddRequest request) {
    return ApiResponse.ok(cartService.addItem(userId, request));
  }

  @Operation(summary = "장바구니 상품 수량 변경")
  @PatchMapping("/items/{cartItemId}")
  public ApiResponse<CartResponse> changeQuantity(@AuthenticationPrincipal Long userId,
      @PathVariable Long cartItemId,
      @Valid @RequestBody CartItemUpdateRequest request) {
    return ApiResponse.ok(cartService.changeQuantity(userId, cartItemId, request));
  }

  @Operation(summary = "장바구니 상품 옵션 변경")
  @PatchMapping("/items/{cartItemId}/option")
  public ApiResponse<CartResponse> changeOption(@AuthenticationPrincipal Long userId,
      @PathVariable Long cartItemId,
      @Valid @RequestBody CartItemOptionUpdateRequest request) {
    return ApiResponse.ok(cartService.changeOption(userId, cartItemId, request));
  }

  @Operation(summary = "장바구니 상품 삭제")
  @DeleteMapping("/items/{cartItemId}")
  public ApiResponse<Void> removeItem(@AuthenticationPrincipal Long userId,
      @PathVariable Long cartItemId) {
    cartService.removeItem(userId, cartItemId);
    return ApiResponse.ok();
  }

  @Operation(summary = "장바구니 전체 비우기")
  @DeleteMapping("/items")
  public ApiResponse<Void> clearCart(@AuthenticationPrincipal Long userId) {
    cartService.clearCart(userId);
    return ApiResponse.ok();
  }
}
