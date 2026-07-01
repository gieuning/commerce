package com.gieun.commerce.domain.user.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenResponse {

  String accessToken;
  Long userId;

  public static TokenResponse of(String accessToken, Long userId) {
    return TokenResponse.builder()
        .accessToken(accessToken)
        .userId(userId)
        .build();
  }
}
