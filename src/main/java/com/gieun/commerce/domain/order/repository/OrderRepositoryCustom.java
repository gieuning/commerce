package com.gieun.commerce.domain.order.repository;

import com.gieun.commerce.domain.order.entity.Order;
import com.gieun.commerce.domain.order.repository.condition.OrderSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepositoryCustom {

  Page<Order> search(Long userId, OrderSearchCondition condition, Pageable pageable);
}
