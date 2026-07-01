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

  // 결제 승인 시점의 권위 있는 차감 (락 잡고 진짜 차감 — restore와 동일한 락 순서로 데드락 방지)
  public void decrease(Order order) {
    for (OrderItem item : sortOrderItemsByStockLockOrder(order.getItems())) {
      if (item.getOptionCombinationId() == null) {
        Product product = productRepository.findByIdForUpdate(item.getProductId())
            .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_PRODUCT));

        product.decreaseStock(item.getQuantity());
        continue;
      }

      OptionCombination combination = combinationRepository
          .findByIdAndProductIdForUpdate(item.getOptionCombinationId(), item.getProductId())
          .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_OPTION_COMBINATION));

      combination.decreaseStock(item.getQuantity());
    }
  }

  // PG 호출 전 빠른 사전 체크 (논락, best-effort — 명백한 품절을 헛결제 전에 거른다)
  public void ensureAvailable(Order order) {
    for (OrderItem item : order.getItems()) {
      if (item.getOptionCombinationId() == null) {
        Product product = productRepository.findById(item.getProductId())
            .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_PRODUCT));

        if (product.getStock() < item.getQuantity()) {
          throw new DomainException(DomainExceptionCode.OUT_OF_STOCK_PRODUCT);
        }
        continue;
      }

      OptionCombination combination = combinationRepository
          .findByIdAndProductId(item.getOptionCombinationId(), item.getProductId())
          .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_OPTION_COMBINATION));

      if (combination.getStock() < item.getQuantity()) {
        throw new DomainException(DomainExceptionCode.OUT_OF_STOCK_OPTION_COMBINATION);
      }
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
