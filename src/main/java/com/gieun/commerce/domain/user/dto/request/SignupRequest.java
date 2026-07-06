package com.gieun.commerce.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SignupRequest {

  @NotBlank(message = "이메일을 입력해 주세요.")
  @Email(message = "올바른 이메일 형식이 아닙니다.")
  String email;

  @NotBlank(message = "비밀번호를 입력해 주세요.")
  @Size(min = 8, max = 72, message = "비밀번호는 8자 이상 72자 이하여야 합니다.")
  String password;

  @NotBlank(message = "이름을 입력해 주세요.")
  @Pattern(regexp = "^[가-힣a-zA-Z\\s]+$", message = "이름에는 한글 또는 영문만 입력할 수 있습니다.")
  String name;

  // 선택 입력. 값이 있으면 '-' 없는 숫자 10~11자리여야 한다(null은 허용).
  @Pattern(regexp = "^\\d{10,11}$", message = "휴대폰 번호는 '-' 없이 숫자 10~11자리로 입력해 주세요.")
  String phoneNumber;
}
