package com.gieun.commerce.global.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum DomainExceptionCode {

  // 인증/인가
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 토큰입니다."),
  EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
  MISSING_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 누락되었습니다."),
  UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "인증되지 않은 접근입니다."),
  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
  JSON_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Json 데이터 처리 중 에러가 발생하였습니다."),

  // 공통
  NOT_FOUND_USER(HttpStatus.NOT_FOUND, "요청한 사용자를 찾을 수 없습니다."),
  DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다."),
  PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다."),
  SAME_AS_OLD_PASSWORD(HttpStatus.BAD_REQUEST, "새 비밀번호는 기존 비밀번호와 달라야 합니다."),

  // 상품
  NOT_FOUND_PRODUCT(HttpStatus.NOT_FOUND, "요청한 상품을 찾을 수 없습니다."),
  OUT_OF_STOCK_PRODUCT(HttpStatus.BAD_REQUEST, "상품의 재고가 부족합니다."),
  UNAVAILABLE_PRODUCT(HttpStatus.BAD_REQUEST, "현재 판매 중이지 않은 상품입니다."),

  // 상품 옵션
  NOT_FOUND_OPTION_COMBINATION(HttpStatus.NOT_FOUND, "요청한 옵션 조합을 찾을 수 없습니다."),
  INVALID_OPTION_REQUEST(HttpStatus.BAD_REQUEST, "옵션 구성이 올바르지 않습니다."),
  OUT_OF_STOCK_OPTION_COMBINATION(HttpStatus.BAD_REQUEST, "옵션 조합의 재고가 부족합니다."),
  DUPLICATE_OPTION_COMBINATION(HttpStatus.BAD_REQUEST, "중복된 옵션 조합이 있습니다."),
  PRODUCT_HAS_OPTIONS(HttpStatus.BAD_REQUEST, "옵션 상품은 조합 단위로 재고를 관리합니다."),

  // 장바구니
  NOT_FOUND_CART(HttpStatus.NOT_FOUND, "요청한 장바구니를 찾을 수 없습니다."),
  NOT_FOUND_CART_ITEM(HttpStatus.NOT_FOUND, "요청한 장바구니 상품을 찾을 수 없습니다."),

  // 주문
  NOT_FOUND_ORDER(HttpStatus.NOT_FOUND, "요청한 주문을 찾을 수 없습니다."),
  UNAUTHORIZED_ORDER(HttpStatus.FORBIDDEN, "해당 주문에 접근 권한이 없습니다."),
  INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "현재 주문 상태에서는 해당 작업을 수행할 수 없습니다."),
  CANNOT_CANCEL_ORDER(HttpStatus.BAD_REQUEST, "취소할 수 없는 주문 상태입니다."),
  EMPTY_ORDER_ITEMS(HttpStatus.BAD_REQUEST, "주문 상품이 비어있습니다."),
  NOT_CANCELLED_ORDER(HttpStatus.BAD_REQUEST, "취소된 주문이 아닙니다."),
  INVALID_ORDER_SEARCH_CONDITION(HttpStatus.BAD_REQUEST, "주문 검색 조건이 올바르지 않습니다."),
  INVALID_SORT_PROPERTY(HttpStatus.BAD_REQUEST, "지원하지 않는 정렬 필드입니다."),

  // 결제
  NOT_FOUND_PAYMENT(HttpStatus.NOT_FOUND, "요청한 결제를 찾을 수 없습니다."),
  NOT_FOUND_PAYMENT_RECEIPT(HttpStatus.NOT_FOUND, "요청한 결제 영수증을 찾을 수 없습니다."),
  CANNOT_REQUEST_PAYMENT(HttpStatus.BAD_REQUEST, "결제 요청을 할 수 없는 주문 상태입니다."),
  CANNOT_CONFIRM_PAYMENT(HttpStatus.BAD_REQUEST, "승인할 수 없는 결제 상태입니다."),
  CANNOT_CANCEL_PAYMENT(HttpStatus.BAD_REQUEST, "취소할 수 없는 결제 상태입니다."),
  ALREADY_REQUESTED_PAYMENT(HttpStatus.CONFLICT, "이미 결제 요청이 존재합니다."),
  INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "결제 금액이 올바르지 않습니다."),
  PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 주문 금액과 일치하지 않습니다."),
  INVALID_PAYMENT_KEY(HttpStatus.BAD_REQUEST, "결제 키가 올바르지 않습니다."),
  DUPLICATE_PAYMENT_EVENT(HttpStatus.CONFLICT, "이미 처리된 결제 이벤트입니다."),
  PAYMENT_APPROVAL_FAILED(HttpStatus.BAD_GATEWAY, "PG 결제 승인에 실패했습니다."),
  PAYMENT_CANCEL_FAILED(HttpStatus.BAD_GATEWAY, "PG 결제 취소에 실패했습니다."),

  ;

  final HttpStatus status;
  final String message;
}
