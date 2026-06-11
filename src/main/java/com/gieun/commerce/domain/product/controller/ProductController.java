package com.gieun.commerce.domain.product.controller;

import com.gieun.commerce.domain.product.dto.request.ProductCreateRequest;
import com.gieun.commerce.domain.product.dto.request.ProductUpdateRequest;
import com.gieun.commerce.domain.product.dto.request.StockUpdateRequest;
import com.gieun.commerce.domain.product.dto.response.ProductResponse;
import com.gieun.commerce.domain.product.service.ProductService;
import com.gieun.commerce.global.response.ApiResponse;
import com.gieun.commerce.global.response.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Product", description = "상품 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

  private final ProductService productService;

  @Operation(summary = "상품 목록 조회", description = "이름 검색(keyword) + 페이징. 판매 중지 상품 제외.")
  @GetMapping
  public ApiResponse<PageResult<ProductResponse>> getProducts(
      @RequestParam(required = false) String keyword,
      @PageableDefault(size = 10) Pageable pageable) {
    return ApiResponse.ok(productService.getList(keyword, pageable));
  }

  @Operation(summary = "상품 상세 조회")
  @GetMapping("/{id}")
  public ApiResponse<ProductResponse> getProduct(@PathVariable Long id) {
    return ApiResponse.ok(productService.getDetail(id));
  }

  @Operation(summary = "상품 등록 (ADMIN)")
  @SecurityRequirement(name = "JWT")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request) {
    return ApiResponse.ok(productService.create(request));
  }

  @Operation(summary = "상품 수정 (ADMIN)")
  @SecurityRequirement(name = "JWT")
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public ApiResponse<ProductResponse> update(@PathVariable Long id,
      @Valid @RequestBody ProductUpdateRequest request) {
    return ApiResponse.ok(productService.update(id, request));
  }

  @Operation(summary = "상품 재고 수정 (ADMIN)")
  @SecurityRequirement(name = "JWT")
  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{id}/stock")
  public ApiResponse<ProductResponse> updateStock(@PathVariable Long id,
      @Valid @RequestBody StockUpdateRequest request) {
    return ApiResponse.ok(productService.updateStock(id, request));
  }

  @Operation(summary = "상품 삭제 (ADMIN, soft delete)")
  @SecurityRequirement(name = "JWT")
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable Long id) {
    productService.delete(id);
    return ApiResponse.ok();
  }
}
