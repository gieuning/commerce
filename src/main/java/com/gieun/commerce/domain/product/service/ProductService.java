package com.gieun.commerce.domain.product.service;

import com.gieun.commerce.domain.product.dto.request.ProductCreateRequest;
import com.gieun.commerce.domain.product.dto.request.ProductUpdateRequest;
import com.gieun.commerce.domain.product.dto.request.StockUpdateRequest;
import com.gieun.commerce.domain.product.dto.response.ProductResponse;
import com.gieun.commerce.domain.product.entity.Product;
import com.gieun.commerce.domain.product.repository.ProductRepository;
import com.gieun.commerce.global.exception.DomainException;
import com.gieun.commerce.global.exception.DomainExceptionCode;
import com.gieun.commerce.global.response.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

  private final ProductRepository productRepository;

  @Transactional
  public ProductResponse create(ProductCreateRequest request) {
    Product product = Product.create(
        request.getName(),
        request.getDescription(),
        request.getPrice(),
        request.getStock(),
        request.getImageUrl()
    );
    return ProductResponse.from(productRepository.save(product));
  }

  public PageResult<ProductResponse> getList(String keyword, Pageable pageable) {
    return new PageResult<>(
        productRepository.search(keyword, pageable).map(ProductResponse::from));
  }

  public ProductResponse getDetail(Long id) {
    return ProductResponse.from(findProduct(id));
  }

  @Transactional
  public ProductResponse update(Long id, ProductUpdateRequest request) {
    Product product = findProduct(id);
    product.update(request.getName(), request.getDescription(), request.getPrice(),
        request.getImageUrl());
    return ProductResponse.from(product);
  }

  @Transactional
  public ProductResponse updateStock(Long id, StockUpdateRequest request) {
    Product product = findProduct(id);
    product.updateStock(request.getStock());
    return ProductResponse.from(product);
  }

  @Transactional
  public void delete(Long id) {
    findProduct(id).discontinue();
  }

  private Product findProduct(Long id) {
    return productRepository.findById(id)
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_PRODUCT));
  }
}
