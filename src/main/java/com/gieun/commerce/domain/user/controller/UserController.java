package com.gieun.commerce.domain.user.controller;

import com.gieun.commerce.domain.user.dto.request.LoginRequest;
import com.gieun.commerce.domain.user.dto.request.SignupRequest;
import com.gieun.commerce.domain.user.dto.response.TokenResponse;
import com.gieun.commerce.domain.user.dto.response.UserResponse;
import com.gieun.commerce.domain.user.service.UserService;
import com.gieun.commerce.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "회원 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

  private final UserService userService;

  @Operation(summary = "회원가입", description = "이메일/비밀번호/이름으로 회원을 등록한다.")
  @PostMapping("/signup")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
    return ApiResponse.ok(userService.signup(request));
  }

  @Operation(summary = "로그인", description = "이메일/비밀번호로 인증하고 Access Token(JWT)을 발급한다.")
  @PostMapping("/login")
  public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
    return ApiResponse.ok(userService.login(request));
  }

  @SecurityRequirement(name = "JWT")
  @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회한다.")
  @GetMapping("/me")
  public ApiResponse<UserResponse> getMyInfo(@AuthenticationPrincipal Long userId) {
    return ApiResponse.ok(userService.getMyInfo(userId));
  }
}
