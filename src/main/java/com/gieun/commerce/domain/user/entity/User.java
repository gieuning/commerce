package com.gieun.commerce.domain.user.entity;

import com.gieun.commerce.global.common.BaseEntity;
import com.gieun.commerce.global.constants.StatusType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;


@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Column(nullable = false, unique = true)
  String email;

  @Column(nullable = false)
  String password;

  @Column(nullable = false)
  String name;

  String phoneNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  StatusType status;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  Role role;

  public static User create(String email, String password, String name, String phoneNumber) {
    return User.builder()
        .email(email)
        .password(password)
        .name(name)
        .phoneNumber(phoneNumber)
        .status(StatusType.ACTIVE)
        .role(Role.USER)
        .build();
  }

  public void updateProfile(String name, String phoneNumber) {
    this.name = name;
    this.phoneNumber = phoneNumber;
  }

  public void changePassword(String encodedPassword) {
    this.password = encodedPassword;
  }

  public void withdraw() {
    this.status = StatusType.DELETED;
  }
}

