package com.gieun.commerce.domain.order.service;

import com.gieun.commerce.domain.order.entity.Order;
import com.gieun.commerce.domain.order.entity.OrderItem;
import com.gieun.commerce.domain.product.entity.OptionCombination;
import com.gieun.commerce.domain.product.entity.Product;
import com.gieun.commerce.domain.product.repository.OptionCombinationRepository;
import com.gieun.commerce.domain.product.repository.ProductRepository;
import com.gieun.commerce.global.exception.DomainException;
import com.gieun.commerce.global.exception.DomainExceptionCode;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderStockService {

  private final ProductRepository productRepository;
  private final OptionCombinationRepository combinationRepository;

  public void restore(Order order) {
    for (OrderItem item : sortOrderItemsByStockLockOrder(order.getItems())) {
      if (item.getOptionCombinationId() == null) {
        Product product = productRepository.findByIdForUpdate(item.getProductId())
            .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_PRODUCT));

        product.increaseStock(item.getQuantity());
        continue;
      }

      OptionCombination combination = combinationRepository
          .findByIdAndProductIdForUpdate(item.getOptionCombinationId(), item.getProductId())
          .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_OPTION_COMBINATION));

      combination.increaseStock(item.getQuantity());
    }
  }

  private List<OrderItem> sortOrderItemsByStockLockOrder(List<OrderItem> items) {
    return items.stream()
        .sorted(Comparator
            .comparing(OrderItem::getProductId)
            .thenComparing(
                OrderItem::getOptionCombinationId,
                Comparator.nullsFirst(Comparator.naturalOrder())
            )
        )
        .toList();
  }
}
