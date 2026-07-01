package com.gieun.commerce.domain.cart.service;

import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * 장바구니 소유자. 회원(userId) 또는 게스트(guestToken) 중 정확히 하나를 가진다.
 * (carts 테이블의 chk_carts_owner: user_id XOR guest_token 과 동일한 불변식)
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartOwner {

  Long userId;
  String guestToken;

  private CartOwner(Long userId, String guestToken) {
    this.userId = userId;
    this.guestToken = guestToken;
  }

  public static CartOwner user(Long userId) {
    Objects.requireNonNull(userId, "회원 ID는 필수입니다.");
    return new CartOwner(userId, null);
  }

  public static CartOwner guest(String guestToken) {
    Objects.requireNonNull(guestToken, "게스트 토큰은 필수입니다.");
    return new CartOwner(null, guestToken);
  }

  public boolean isGuest() {
    return userId == null;
  }
}
