package com.gieun.commerce.domain.product.repository;

import com.gieun.commerce.domain.product.entity.Product;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {
  Page<Product> search(String keyword, Pageable pageable);

  Optional<Product> findDetailById(Long id);
}
