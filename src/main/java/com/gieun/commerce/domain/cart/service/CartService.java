package com.gieun.commerce.domain.cart.service;

import com.gieun.commerce.domain.cart.dto.request.CartItemAddRequest;
import com.gieun.commerce.domain.cart.dto.request.CartItemOptionUpdateRequest;
import com.gieun.commerce.domain.cart.dto.request.CartItemUpdateRequest;
import com.gieun.commerce.domain.cart.dto.response.CartItemResponse;
import com.gieun.commerce.domain.cart.dto.response.CartResponse;
import com.gieun.commerce.domain.cart.dto.response.CartSummary;
import com.gieun.commerce.domain.cart.entity.Cart;
import com.gieun.commerce.domain.cart.entity.CartItem;
import com.gieun.commerce.domain.cart.repository.CartRepository;
import com.gieun.commerce.domain.product.entity.OptionCombination;
import com.gieun.commerce.domain.product.entity.Product;
import com.gieun.commerce.domain.product.entity.ProductStatus;
import com.gieun.commerce.domain.product.repository.OptionCombinationRepository;
import com.gieun.commerce.domain.product.repository.OptionGroupRepository;
import com.gieun.commerce.domain.product.repository.ProductRepository;
import com.gieun.commerce.global.exception.DomainException;
import com.gieun.commerce.global.exception.DomainExceptionCode;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

  private final CartRepository cartRepository;
  private final ProductRepository productRepository;
  private final OptionGroupRepository optionGroupRepository;
  private final OptionCombinationRepository optionCombinationRepository;
  private final TransactionTemplate transactionTemplate;

  public CartResponse getCart(Long userId) {
    return cartRepository.findByUserId(userId)
        .map(this::toResponse)
        .orElseGet(CartResponse::empty);
  }

  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public CartResponse addItem(Long userId, CartItemAddRequest request) {
    try {
      return executeAddItem(userId, request);
    } catch (DataIntegrityViolationException e) {
      return executeAddItem(userId, request);
    }
  }

  private CartResponse executeAddItem(Long userId, CartItemAddRequest request) {
    return Objects.requireNonNull(
        transactionTemplate.execute(status -> addItemOnce(userId, request))
    );
  }

  private CartResponse addItemOnce(Long userId, CartItemAddRequest request) {
    Cart cart = cartRepository.findByUserId(userId)
        .orElseGet(() -> Cart.forUser(userId));

    Product product = productRepository.findById(request.getProductId())
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_PRODUCT));

    int quantityAfterAdd = cart.calculateQuantityAfterAdd(
        product.getId(),
        request.getOptionCombinationId(),
        request.getQuantity()
    );

    OptionCombination combination = validateAddItem(product, request.getOptionCombinationId(),
        quantityAfterAdd);

    cart.addItem(
        product.getId(),
        combination == null ? null : combination.getId(),
        request.getQuantity()
    );

    Cart savedCart = cartRepository.save(cart);
    return toResponse(savedCart);
  }

  @Transactional
  public void removeItem(Long userId, Long cartItemId) {
    Cart cart = findCart(userId);
    cart.removeItem(cartItemId);
  }

  @Transactional
  public void clearCart(Long userId) {
    cartRepository.findByUserId(userId)
        .ifPresent(Cart::clear);
  }

  @Transactional
  public CartResponse changeQuantity(Long userId, Long cartItemId, CartItemUpdateRequest request) {
    Cart cart = findCart(userId);

    CartItem item = cart.getItem(cartItemId);
    validateQuantityChange(item, request.getQuantity());

    cart.changeQuantity(item, request.getQuantity());

    return toResponse(cart);
  }

  @Transactional
  public CartResponse changeOption(Long userId, Long cartItemId, CartItemOptionUpdateRequest request) {
    Cart cart = findCart(userId);

    CartItem item = cart.getItem(cartItemId);
    Product product = productRepository.findById(item.getProductId())
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_PRODUCT));

    if (product.getStatus() != ProductStatus.FOR_SALE) {
      throw new DomainException(DomainExceptionCode.UNAVAILABLE_PRODUCT);
    }

    int quantityAfterOptionChange = cart.calculateQuantityAfterOptionChange(
        item,
        request.getOptionCombinationId()
    );
    validateOptionProduct(product, request.getOptionCombinationId(), quantityAfterOptionChange);

    cart.changeOption(item, request.getOptionCombinationId());

    return toResponse(cart);
  }


  private Cart findCart(Long userId) {
    return cartRepository.findByUserId(userId)
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_CART));
  }

  private void validateQuantityChange(CartItem item, int quantity) {
    Product product = productRepository.findById(item.getProductId())
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_PRODUCT));

    if (product.getStatus() != ProductStatus.FOR_SALE) {
      throw new DomainException(DomainExceptionCode.UNAVAILABLE_PRODUCT);
    }

    if (item.getOptionCombinationId() == null) {
      validateSingleProduct(product, quantity);
      return;
    }

    validateOptionProduct(product, item.getOptionCombinationId(), quantity);
  }


  private CartResponse toResponse(Cart cart) {
    List<CartItem> items = cart.getItems();
    if (items.isEmpty()) {
      return CartResponse.empty();
    }

    Map<Long, Product> products = findProducts(items);
    Map<Long, OptionCombination> combinations = findOptionCombinations(items);

    List<CartItemResponse> itemResponses = items.stream()
        .map(item -> CartItemResponse.of(
            item,
            products.get(item.getProductId()),
            item.getOptionCombinationId() == null
                ? null
                : combinations.get(item.getOptionCombinationId())
        )).toList();

    CartSummary summary = calculateSummary(itemResponses);
    return CartResponse.of(itemResponses, summary);
  }


  private Map<Long, Product> findProducts(List<CartItem> items) {
    Set<Long> productIds = items.stream()
        .map(CartItem::getProductId)
        .collect(Collectors.toSet());

    return productRepository.findAllById(productIds).stream()
        .collect(Collectors.toMap(Product::getId, Function.identity()));
  }

  private Map<Long, OptionCombination> findOptionCombinations(List<CartItem> items) {
    Set<Long> combinationIds = items.stream()
        .map(CartItem::getOptionCombinationId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());

    if (combinationIds.isEmpty()) {
      return Map.of();
    }

    return optionCombinationRepository.findAllWithValuesByIdIn(combinationIds).stream()
        .collect(Collectors.toMap(OptionCombination::getId, Function.identity()));
  }


  private CartSummary calculateSummary(List<CartItemResponse> items) {
    BigDecimal totalPrice = items.stream()
        .filter(CartItemResponse::isAvailable)
        .map(CartItemResponse::getSubtotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    int totalQuantity = items.stream()
        .filter(CartItemResponse::isAvailable)
        .mapToInt(CartItemResponse::getQuantity)
        .sum();

    return CartSummary.builder()
        .totalQuantity(totalQuantity)
        .totalPrice(totalPrice)
        .build();
  }

  private OptionCombination validateAddItem(Product product, Long optionCombinationId, int quantity) {
    if (product.getStatus() != ProductStatus.FOR_SALE) {
      throw new DomainException(DomainExceptionCode.UNAVAILABLE_PRODUCT);
    }

    if (optionCombinationId == null) {
      validateSingleProduct(product, quantity);
      return null;
    }

    return validateOptionProduct(product, optionCombinationId, quantity);
  }

  // 단품 상품 검증
  private void validateSingleProduct(Product product, int quantity) {
    if (optionGroupRepository.existsByProductId(product.getId())) {
      throw new DomainException(DomainExceptionCode.PRODUCT_HAS_OPTIONS);
    }

    if (product.getStock() < quantity) {
      throw new DomainException(DomainExceptionCode.OUT_OF_STOCK_PRODUCT);
    }
  }

  private OptionCombination validateOptionProduct(
      Product product,
      Long optionCombinationId,
      int quantity
  ) {
    OptionCombination combination = optionCombinationRepository.findByIdAndProductId(
            optionCombinationId, product.getId())
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_OPTION_COMBINATION));

    if (combination.getStatus() != ProductStatus.FOR_SALE) {
      throw new DomainException(DomainExceptionCode.UNAVAILABLE_PRODUCT);
    }

    if (combination.getStock() < quantity) {
      throw new DomainException(DomainExceptionCode.OUT_OF_STOCK_OPTION_COMBINATION);
    }

    return combination;
  }


}
