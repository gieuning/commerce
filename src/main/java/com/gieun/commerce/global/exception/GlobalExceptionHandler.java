package com.gieun.commerce.global.exception;

import com.gieun.commerce.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String VALIDATION_ERROR = "VALIDATION_ERROR";
  private static final String SERVER_ERROR = "SERVER_ERROR";

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException ex) {
    log.warn("[DomainException] : code={}, message={}", ex.getCode(), ex.getMessage());
    return ApiResponse.fail(ex.getHttpStatus(), ex.getCode(), ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex) {
    String errorMessage = extractErrorMessages(ex);
    log.warn("[ValidationException] : {}", errorMessage);
    return ApiResponse.fail(HttpStatus.BAD_REQUEST, VALIDATION_ERROR, errorMessage);
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<ApiResponse<Void>> handleBindException(BindException ex) {
    String errorMessage = extractErrorMessages(ex);
    log.warn("[BindException] : {}", errorMessage);
    return ApiResponse.fail(HttpStatus.BAD_REQUEST, VALIDATION_ERROR, errorMessage);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex) {
    log.warn("[HttpMessageNotReadableException] 요청 본문(JSON) 형식 오류");
    return ApiResponse.fail(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_BODY",
        "요청 본문(JSON) 형식이 올바르지 않습니다.");
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(NoResourceFoundException ex) {
    return ApiResponse.fail(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
    log.error("[Exception] : ", ex);
    String message = ex.getMessage() != null ? ex.getMessage() : "서버 오류가 발생하였습니다.";
    return ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR, SERVER_ERROR, message);
  }

  private String extractErrorMessages(BindException ex) {
    return ex.getBindingResult()
        .getAllErrors()
        .stream()
        .map(DefaultMessageSourceResolvable::getDefaultMessage)
        .collect(Collectors.joining(", "));
  }
}