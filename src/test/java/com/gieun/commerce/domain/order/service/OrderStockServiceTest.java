package com.gieun.commerce.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gieun.commerce.domain.order.entity.Order;
import com.gieun.commerce.domain.order.entity.OrderItem;
import com.gieun.commerce.domain.product.entity.OptionCombination;
import com.gieun.commerce.domain.product.entity.Product;
import com.gieun.commerce.domain.product.repository.OptionCombinationRepository;
import com.gieun.commerce.domain.product.repository.ProductRepository;
import com.gieun.commerce.global.exception.DomainException;
import com.gieun.commerce.global.exception.DomainExceptionCode;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderStockServiceTest {

  @Mock
  ProductRepository productRepository;

  @Mock
  OptionCombinationRepository combinationRepository;

  @InjectMocks
  OrderStockService orderStockService;

  @Test
  void restoreRejectsOptionCombinationThatDoesNotBelongToOrderItemProduct() {
    Long productId = 1L;
    Long optionCombinationId = 10L;
    int quantity = 2;
    Order order = Order.create(100L);
    order.addItem(OrderItem.create(
        productId,
        optionCombinationId,
        "상품",
        "색상: 검정",
        new BigDecimal("10000.00"),
        quantity
    ));
    OptionCombination mismatchedCombination = OptionCombination.create(BigDecimal.ZERO, 5);

    lenient().when(combinationRepository.findByIdForUpdate(optionCombinationId))
        .thenReturn(Optional.of(mismatchedCombination));
    when(combinationRepository.findByIdAndProductIdForUpdate(optionCombinationId, productId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderStockService.restore(order))
        .isInstanceOfSatisfying(DomainException.class, exception ->
            assertThat(exception.getCode()).isEqualTo(DomainExceptionCode.NOT_FOUND_OPTION_COMBINATION.name()));
    assertThat(mismatchedCombination.getStock()).isEqualTo(5);
    verify(combinationRepository).findByIdAndProductIdForUpdate(optionCombinationId, productId);
  }

  @Test
  void decreaseDoesNotPartiallyDecrementWhenLaterItemIsOutOfStock() {
    // 다품목 주문: 앞 상품(재고 충분) + 뒤 상품(품절)
    Product available = product(1L, 10);
    Product shortage = product(2L, 1);
    Order order = Order.create(100L);
    order.addItem(OrderItem.create(1L, null, "상품1", null, new BigDecimal("10000.00"), 3));
    order.addItem(OrderItem.create(2L, null, "상품2", null, new BigDecimal("10000.00"), 5));

    when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(available));
    when(productRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(shortage));

    assertThatThrownBy(() -> orderStockService.decrease(order))
        .isInstanceOfSatisfying(DomainException.class, exception ->
            assertThat(exception.getCode()).isEqualTo(DomainExceptionCode.OUT_OF_STOCK_PRODUCT.name()));

    // 핵심: 뒤 아이템 품절로 실패했어도 앞 아이템 재고는 그대로여야 한다 (부분 차감 방지)
    assertThat(available.getStock()).isEqualTo(10);
    assertThat(shortage.getStock()).isEqualTo(1);
  }

  @Test
  void decreaseAppliesAllWhenEveryItemHasStock() {
    Product first = product(1L, 10);
    Product second = product(2L, 8);
    Order order = Order.create(100L);
    order.addItem(OrderItem.create(1L, null, "상품1", null, new BigDecimal("10000.00"), 3));
    order.addItem(OrderItem.create(2L, null, "상품2", null, new BigDecimal("10000.00"), 5));

    when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(first));
    when(productRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(second));

    orderStockService.decrease(order);

    assertThat(first.getStock()).isEqualTo(7);
    assertThat(second.getStock()).isEqualTo(3);
  }

  private Product product(Long id, int stock) {
    return Product.builder()
        .id(id)
        .stock(stock)
        .build();
  }
}
