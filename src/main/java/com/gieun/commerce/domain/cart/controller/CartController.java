package com.gieun.commerce.domain.cart.controller;

import com.gieun.commerce.domain.cart.dto.request.CartItemAddRequest;
import com.gieun.commerce.domain.cart.dto.request.CartItemOptionUpdateRequest;
import com.gieun.commerce.domain.cart.dto.request.CartItemUpdateRequest;
import com.gieun.commerce.domain.cart.dto.response.CartResponse;
import com.gieun.commerce.domain.cart.service.CartOwner;
import com.gieun.commerce.domain.cart.service.CartService;
import com.gieun.commerce.global.exception.DomainException;
import com.gieun.commerce.global.exception.DomainExceptionCode;
import com.gieun.commerce.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cart", description = "장바구니 API (회원: JWT / 게스트: X-Guest-Token)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

  private static final String GUEST_TOKEN_HEADER = "X-Guest-Token";

  private final CartService cartService;

  @Operation(summary = "내 장바구니 조회")
  @GetMapping
  public ApiResponse<CartResponse> getCart(
      @AuthenticationPrincipal Long userId,
      @RequestHeader(value = GUEST_TOKEN_HEADER, required = false) String guestToken) {
    CartOwner owner = resolveOwner(userId, guestToken);
    return ApiResponse.ok(owner == null ? CartResponse.empty() : cartService.getCart(owner));
  }

  @Operation(summary = "장바구니 상품 추가 (게스트는 X-Guest-Token 응답 헤더로 발급)")
  @PostMapping("/items")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<CartResponse> addItem(
      @AuthenticationPrincipal Long userId,
      @RequestHeader(value = GUEST_TOKEN_HEADER, required = false) String guestToken,
      @Valid @RequestBody CartItemAddRequest request,
      HttpServletResponse response) {
    CartOwner owner = resolveOwnerForWrite(userId, guestToken, response);
    return ApiResponse.ok(cartService.addItem(owner, request));
  }

  @Operation(summary = "장바구니 상품 수량 변경")
  @PatchMapping("/items/{cartItemId}")
  public ApiResponse<CartResponse> changeQuantity(
      @AuthenticationPrincipal Long userId,
      @RequestHeader(value = GUEST_TOKEN_HEADER, required = false) String guestToken,
      @PathVariable Long cartItemId,
      @Valid @RequestBody CartItemUpdateRequest request) {
    return ApiResponse.ok(cartService.changeQuantity(requireOwner(userId, guestToken), cartItemId, request));
  }

  @Operation(summary = "장바구니 상품 옵션 변경")
  @PatchMapping("/items/{cartItemId}/option")
  public ApiResponse<CartResponse> changeOption(
      @AuthenticationPrincipal Long userId,
      @RequestHeader(value = GUEST_TOKEN_HEADER, required = false) String guestToken,
      @PathVariable Long cartItemId,
      @Valid @RequestBody CartItemOptionUpdateRequest request) {
    return ApiResponse.ok(cartService.changeOption(requireOwner(userId, guestToken), cartItemId, request));
  }

  @Operation(summary = "장바구니 상품 삭제")
  @DeleteMapping("/items/{cartItemId}")
  public ApiResponse<Void> removeItem(
      @AuthenticationPrincipal Long userId,
      @RequestHeader(value = GUEST_TOKEN_HEADER, required = false) String guestToken,
      @PathVariable Long cartItemId) {
    cartService.removeItem(requireOwner(userId, guestToken), cartItemId);
    return ApiResponse.ok();
  }

  @Operation(summary = "장바구니 전체 비우기")
  @DeleteMapping("/items")
  public ApiResponse<Void> clearCart(
      @AuthenticationPrincipal Long userId,
      @RequestHeader(value = GUEST_TOKEN_HEADER, required = false) String guestToken) {
    cartService.clearCart(requireOwner(userId, guestToken));
    return ApiResponse.ok();
  }

  // 회원(JWT) 우선, 없으면 게스트 토큰. 둘 다 없으면 null(소유자 미상).
  private CartOwner resolveOwner(Long userId, String guestToken) {
    if (userId != null) {
      return CartOwner.user(userId);
    }
    if (hasText(guestToken)) {
      return CartOwner.guest(guestToken);
    }
    return null;
  }

  // 조회/수정: 소유자가 없으면 장바구니 없음.
  private CartOwner requireOwner(Long userId, String guestToken) {
    CartOwner owner = resolveOwner(userId, guestToken);
    if (owner == null) {
      throw new DomainException(DomainExceptionCode.NOT_FOUND_CART);
    }
    return owner;
  }

  // 담기: 게스트인데 토큰이 없으면 새로 발급하고 응답 헤더로 반환한다.
  private CartOwner resolveOwnerForWrite(Long userId, String guestToken, HttpServletResponse response) {
    CartOwner owner = resolveOwner(userId, guestToken);
    if (owner != null) {
      return owner;
    }
    String newToken = UUID.randomUUID().toString();
    response.setHeader(GUEST_TOKEN_HEADER, newToken);
    return CartOwner.guest(newToken);
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}
