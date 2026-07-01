package com.gieun.commerce.domain.order.service;

import com.gieun.commerce.domain.order.dto.request.OrderCreateRequest;
import com.gieun.commerce.domain.order.dto.request.OrderItemRequest;
import com.gieun.commerce.domain.order.dto.request.OrderSearchRequest;
import com.gieun.commerce.domain.order.dto.response.OrderResponse;
import com.gieun.commerce.domain.order.entity.Order;
import com.gieun.commerce.domain.order.entity.OrderItem;
import com.gieun.commerce.domain.order.repository.OrderRepository;
import com.gieun.commerce.domain.order.repository.condition.OrderSearchCondition;
import com.gieun.commerce.domain.product.entity.OptionCombination;
import com.gieun.commerce.domain.product.entity.Product;
import com.gieun.commerce.domain.product.entity.ProductStatus;
import com.gieun.commerce.domain.product.repository.OptionCombinationRepository;
import com.gieun.commerce.domain.product.repository.ProductRepository;
import com.gieun.commerce.global.exception.DomainException;
import com.gieun.commerce.global.exception.DomainExceptionCode;
import com.gieun.commerce.global.response.PageResult;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final OptionCombinationRepository combinationRepository;
  private final OrderStockService orderStockService;

  @Transactional
  public OrderResponse create(Long userId, OrderCreateRequest request) {
    Order order = Order.create(userId);

    for (OrderItemRequest itemRequest : sortRequestItemsByStockLockOrder(request.getItems())) {
      Product product = productRepository.findByIdForUpdate(itemRequest.getProductId())
          .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_PRODUCT));

      OrderItem orderItem = createOrderItem(product, itemRequest);
      order.addItem(orderItem);
    }

    Order savedOrder = orderRepository.save(order);
    return OrderResponse.of(savedOrder);
  }

  @Transactional
  public OrderResponse cancel(Long userId, Long orderId) {
    Order order = orderRepository.findByIdAndUserIdForUpdate(orderId, userId)
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_ORDER));

    order.cancel();
    orderStockService.restore(order);

    return OrderResponse.of(order);
  }

  private List<OrderItemRequest> sortRequestItemsByStockLockOrder(List<OrderItemRequest> items) {
    return items.stream()
        .sorted(Comparator
            .comparing(OrderItemRequest::getProductId)
            .thenComparing(
                OrderItemRequest::getOptionCombinationId,
                Comparator.nullsFirst(Comparator.naturalOrder())
            )
        )
        .toList();
  }

  public OrderResponse getOrder(Long userId, Long orderId) {
    Order order = orderRepository.findByIdAndUserId(orderId, userId)
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_ORDER));

    return OrderResponse.of(order);
  }

  public PageResult<OrderResponse> getOrders(
      Long userId,
      OrderSearchRequest request,
      Pageable pageable
  ) {
    validateSearchRequest(request);

    OrderSearchCondition condition = request.toCondition();
    return new PageResult<>(
        orderRepository.search(userId, condition, pageable)
            .map(OrderResponse::of)
    );
  }

  private OrderItem createOrderItem(Product product, OrderItemRequest request) {
    if (request.getOptionCombinationId() == null) {
      validateSingleProduct(product, request.getQuantity());
      // 재고 차감은 결제 승인 시점(PaymentService)으로 이동. 여기선 가용성 검증만.

      return OrderItem.create(
          product.getId(),
          null,
          product.getName(),
          null,
          product.getPrice(),
          request.getQuantity()
      );
    }

    OptionCombination combination = combinationRepository
        .findByIdAndProductIdForUpdate(request.getOptionCombinationId(), product.getId())
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_OPTION_COMBINATION));

    validateOptionProduct(product, combination, request.getQuantity());
    // 재고 차감은 결제 승인 시점(PaymentService)으로 이동. 여기선 가용성 검증만.

    return OrderItem.create(
        product.getId(),
        combination.getId(),
        product.getName(),
        buildOptionValues(combination),
        product.getPrice().add(combination.getAdditionalPrice()),
        request.getQuantity()
    );
  }

  private void validateSingleProduct(Product product, int quantity) {
    if (product.getStatus() != ProductStatus.FOR_SALE) {
      throw new DomainException(DomainExceptionCode.UNAVAILABLE_PRODUCT);
    }

    if (product.hasOptions()) {
      throw new DomainException(DomainExceptionCode.PRODUCT_HAS_OPTIONS);
    }

    if (product.getStock() < quantity) {
      throw new DomainException(DomainExceptionCode.OUT_OF_STOCK_PRODUCT);
    }
  }

  private void validateOptionProduct(Product product, OptionCombination combination, int quantity) {
    if (product.getStatus() != ProductStatus.FOR_SALE) {
      throw new DomainException(DomainExceptionCode.UNAVAILABLE_PRODUCT);
    }

    if (combination.getStatus() != ProductStatus.FOR_SALE) {
      throw new DomainException(DomainExceptionCode.UNAVAILABLE_PRODUCT);
    }

    if (combination.getStock() < quantity) {
      throw new DomainException(DomainExceptionCode.OUT_OF_STOCK_OPTION_COMBINATION);
    }
  }


  private String buildOptionValues(OptionCombination combination) {
    return combination.getValues().stream()
        .map(value -> value.getOptionGroup().getName() + ": " + value.getOptionValue().getName())
        .collect(Collectors.joining(" / "));
  }

  private void validateSearchRequest(OrderSearchRequest request) {
    if (request.getFrom() != null
        && request.getTo() != null
        && request.getFrom().isAfter(request.getTo())) {
      throw new DomainException(DomainExceptionCode.INVALID_ORDER_SEARCH_CONDITION);
    }
  }


}
