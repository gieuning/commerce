package com.gieun.commerce.global.security;

import com.gieun.commerce.domain.user.entity.User;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class CustomUserDetails implements UserDetails, Serializable {

  private static final long serialVersionUID = 1;

  private final Long userId;
  private final String email;
  private final String password;
  private final String role;

  public CustomUserDetails(Long userId, String email, String password, String role) {
    this.userId = userId;
    this.email = email;
    this.password = password;
    this.role = role;
  }

  public static CustomUserDetails from(User user) {
    return new CustomUserDetails(user.getId(), user.getEmail(), user.getPassword(),
        user.getRole().name());
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role));
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
