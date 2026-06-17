package com.gieun.commerce.domain.product.repository;

import com.gieun.commerce.domain.product.entity.OptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionGroupRepository extends JpaRepository<OptionGroup, Long> {

  boolean existsByProductId(Long productId);
}
