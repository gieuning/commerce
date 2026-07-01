package com.gieun.commerce.domain.user.controller;

import com.gieun.commerce.domain.user.dto.request.ChangePasswordRequest;
import com.gieun.commerce.domain.user.dto.request.LoginRequest;
import com.gieun.commerce.domain.user.dto.request.SignupRequest;
import com.gieun.commerce.domain.user.dto.request.UpdateProfileRequest;
import com.gieun.commerce.domain.user.dto.request.WithdrawRequest;
import com.gieun.commerce.domain.user.dto.response.TokenResponse;
import com.gieun.commerce.domain.cart.service.CartService;
import com.gieun.commerce.domain.user.dto.response.UserResponse;
import com.gieun.commerce.domain.user.service.UserService;
import com.gieun.commerce.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "User", description = "회원 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

  private static final String GUEST_TOKEN_HEADER = "X-Guest-Token";

  private final UserService userService;
  private final CartService cartService;

  @Operation(summary = "회원가입", description = "이메일/비밀번호/이름으로 회원을 등록한다.")
  @PostMapping("/signup")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<UserResponse> signup(
      @Valid @RequestBody SignupRequest request,
      @RequestHeader(value = GUEST_TOKEN_HEADER, required = false) String guestToken) {
    UserResponse response = userService.signup(request);
    mergeGuestCart(response.getId(), guestToken);
    return ApiResponse.ok(response);
  }

  @Operation(summary = "로그인", description = "이메일/비밀번호로 인증하고 Access Token(JWT)을 발급한다.")
  @PostMapping("/login")
  public ApiResponse<TokenResponse> login(
      @Valid @RequestBody LoginRequest request,
      @RequestHeader(value = GUEST_TOKEN_HEADER, required = false) String guestToken) {
    TokenResponse token = userService.login(request);
    mergeGuestCart(token.getUserId(), guestToken);
    return ApiResponse.ok(token);
  }

  // 게스트 카트 병합은 best-effort — 실패해도 로그인/가입은 성공시킨다.
  private void mergeGuestCart(Long userId, String guestToken) {
    if (guestToken == null || guestToken.isBlank()) {
      return;
    }
    try {
      cartService.merge(userId, guestToken);
    } catch (Exception e) {
      log.warn("게스트 카트 병합 실패 — userId={}, guestToken={}, error={}", userId, guestToken, e.getMessage());
    }
  }

  @SecurityRequirement(name = "JWT")
  @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회한다.")
  @GetMapping("/me")
  public ApiResponse<UserResponse> getMyInfo(@AuthenticationPrincipal Long userId) {
    return ApiResponse.ok(userService.getMyInfo(userId));
  }

  @SecurityRequirement(name = "JWT")
  @Operation(summary = "내 정보 수정", description = "이름, 휴대폰 번호를 수정한다. 이메일은 변경할 수 없다.")
  @PatchMapping("/me")
  public ApiResponse<UserResponse> updateMyInfo(@AuthenticationPrincipal Long userId,
      @Valid @RequestBody UpdateProfileRequest request) {
    return ApiResponse.ok(userService.updateProfile(userId, request));
  }

  @SecurityRequirement(name = "JWT")
  @Operation(summary = "비밀번호 변경", description = "현재 비밀번호 확인 후 새 비밀번호로 변경한다.")
  @PatchMapping("/me/password")
  public ApiResponse<Void> changePassword(@AuthenticationPrincipal Long userId,
      @Valid @RequestBody ChangePasswordRequest request) {
    userService.changePassword(userId, request);
    return ApiResponse.ok();
  }

  @SecurityRequirement(name = "JWT")
  @Operation(summary = "회원 탈퇴", description = "비밀번호 확인 후 계정을 비활성화(탈퇴) 처리한다.")
  @DeleteMapping("/me")
  public ApiResponse<Void> withdraw(@AuthenticationPrincipal Long userId,
      @Valid @RequestBody WithdrawRequest request) {
    userService.withdraw(userId, request);
    return ApiResponse.ok();
  }
}
