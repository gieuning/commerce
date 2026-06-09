package com.gieun.commerce.domain.user.dto.response;

import com.gieun.commerce.domain.user.entity.Role;
import com.gieun.commerce.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {

  Long id;
  String email;
  String name;
  String phoneNumber;
  Role role;

  public static UserResponse from(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .phoneNumber(user.getPhoneNumber())
        .role(user.getRole())
        .build();
  }
}
