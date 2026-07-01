package com.gieun.commerce.domain.user.service;

import com.gieun.commerce.domain.user.dto.request.ChangePasswordRequest;
import com.gieun.commerce.domain.user.dto.request.LoginRequest;
import com.gieun.commerce.domain.user.dto.request.SignupRequest;
import com.gieun.commerce.domain.user.dto.request.UpdateProfileRequest;
import com.gieun.commerce.domain.user.dto.request.WithdrawRequest;
import com.gieun.commerce.domain.user.dto.response.TokenResponse;
import com.gieun.commerce.domain.user.dto.response.UserResponse;
import com.gieun.commerce.domain.user.entity.User;
import com.gieun.commerce.domain.user.repository.UserRepository;
import com.gieun.commerce.global.exception.DomainException;
import com.gieun.commerce.global.exception.DomainExceptionCode;
import com.gieun.commerce.global.security.CustomUserDetails;
import com.gieun.commerce.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;

  @Transactional
  public UserResponse signup(SignupRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new DomainException(DomainExceptionCode.DUPLICATE_EMAIL);
    }

    User user = User.create(
        request.getEmail(),
        passwordEncoder.encode(request.getPassword()),
        request.getName(),
        request.getPhoneNumber()
    );

    try {
      return UserResponse.from(userRepository.save(user));
    } catch (DataIntegrityViolationException e) {
      throw new DomainException(DomainExceptionCode.DUPLICATE_EMAIL);
    }
  }

  public TokenResponse login(LoginRequest request) {
    Authentication authentication;
    try {
      authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    } catch (AuthenticationException e) {
      throw new DomainException(DomainExceptionCode.INVALID_CREDENTIALS);
    }

    CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
    String token = jwtTokenProvider.createToken(principal.getUserId(), principal.getRole());
    return TokenResponse.of(token, principal.getUserId());
  }

  public UserResponse getMyInfo(Long userId) {
    return UserResponse.from(getUserById(userId));
  }

  @Transactional
  public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
    User user = getUserById(userId);
    user.updateProfile(request.getName(), request.getPhoneNumber());
    return UserResponse.from(user);
  }

  @Transactional
  public void changePassword(Long userId, ChangePasswordRequest request) {
    User user = getUserById(userId);

    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
      throw new DomainException(DomainExceptionCode.PASSWORD_MISMATCH);
    }
    if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
      throw new DomainException(DomainExceptionCode.SAME_AS_OLD_PASSWORD);
    }

    user.changePassword(passwordEncoder.encode(request.getNewPassword()));
  }

  @Transactional
  public void withdraw(Long userId, WithdrawRequest request) {
    User user = getUserById(userId);

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new DomainException(DomainExceptionCode.PASSWORD_MISMATCH);
    }

    user.withdraw();
  }

  private User getUserById(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_USER));
  }
}
