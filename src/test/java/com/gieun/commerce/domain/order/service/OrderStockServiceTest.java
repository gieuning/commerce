package com.gieun.commerce.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gieun.commerce.domain.order.entity.Order;
import com.gieun.commerce.domain.order.entity.OrderItem;
import com.gieun.commerce.domain.product.entity.OptionCombination;
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
}
