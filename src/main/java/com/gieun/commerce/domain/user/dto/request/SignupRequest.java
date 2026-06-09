package com.gieun.commerce.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

  @NotBlank
  @Email
  String email;

  @NotBlank
  @Size(min = 8, max = 72)
  String password;

  @NotBlank
  String name;

  String phoneNumber;
}
