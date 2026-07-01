package com.gieun.commerce.domain.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gieun.commerce.domain.cart.dto.request.CartItemAddRequest;
import com.gieun.commerce.domain.cart.dto.response.CartResponse;
import com.gieun.commerce.domain.cart.entity.Cart;
import com.gieun.commerce.domain.cart.entity.CartItem;
import com.gieun.commerce.domain.cart.repository.CartRepository;
import com.gieun.commerce.domain.product.entity.Product;
import com.gieun.commerce.domain.product.entity.ProductStatus;
import com.gieun.commerce.domain.product.repository.OptionCombinationRepository;
import com.gieun.commerce.domain.product.repository.OptionGroupRepository;
import com.gieun.commerce.domain.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

  @Mock
  CartRepository cartRepository;

  @Mock
  ProductRepository productRepository;

  @Mock
  OptionGroupRepository optionGroupRepository;

  @Mock
  OptionCombinationRepository optionCombinationRepository;

  @Mock
  TransactionTemplate transactionTemplate;

  @InjectMocks
  CartService cartService;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    lenient().when(transactionTemplate.execute(any())).thenAnswer(invocation ->
        ((TransactionCallback<Object>) invocation.getArgument(0))
            .doInTransaction(mock(TransactionStatus.class)));
  }

  @Test
  void guestAddItemCreatesGuestCart() {
    String guestToken = "guest-token-1";
    Product product = product(1L, 10);
    when(cartRepository.findByGuestToken(guestToken)).thenReturn(Optional.empty());
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    when(optionGroupRepository.existsByProductId(1L)).thenReturn(false);
    when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(productRepository.findAllById(any())).thenReturn(List.of(product));

    CartResponse response = cartService.addItem(
        CartOwner.guest(guestToken),
        CartItemAddRequest.builder().productId(1L).quantity(2).build());

    assertThat(response).isNotNull();
    // 게스트 카트로 생성되었는지 (userId 없음, guestToken 설정, 아이템 1개)
    ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
    org.mockito.Mockito.verify(cartRepository).save(cartCaptor.capture());
    Cart saved = cartCaptor.getValue();
    assertThat(saved.getGuestToken()).isEqualTo(guestToken);
    assertThat(saved.getUserId()).isNull();
    assertThat(saved.getItems()).hasSize(1);
  }

  @Test
  void getCartForGuestReturnsEmptyWhenNoCart() {
    when(cartRepository.findByGuestToken("no-cart")).thenReturn(Optional.empty());

    CartResponse response = cartService.getCart(CartOwner.guest("no-cart"));

    assertThat(response.getItems()).isEmpty();
  }

  @Test
  void mergeConvertsGuestCartWhenUserHasNoCart() {
    Cart guestCart = Cart.forGuest("g1");
    guestCart.addItem(1L, null, 2);
    when(cartRepository.findByGuestToken("g1")).thenReturn(Optional.of(guestCart));
    when(cartRepository.findByUserId(5L)).thenReturn(Optional.empty());

    cartService.merge(5L, "g1");

    // 회원 카트가 없으면 게스트 카트의 주인만 바뀐다 (삭제/이동 없음)
    assertThat(guestCart.getUserId()).isEqualTo(5L);
    assertThat(guestCart.getGuestToken()).isNull();
    verify(cartRepository, never()).delete(any(Cart.class));
  }

  @Test
  void mergeCombinesItemsAndDeletesGuestCartWhenUserCartExists() {
    Cart guestCart = Cart.forGuest("g1");
    guestCart.addItem(1L, null, 2); // 상품1 x2
    Cart userCart = Cart.forUser(5L);
    userCart.addItem(1L, null, 1);  // 상품1 x1
    userCart.addItem(2L, null, 1);  // 상품2 x1
    when(cartRepository.findByGuestToken("g1")).thenReturn(Optional.of(guestCart));
    when(cartRepository.findByUserId(5L)).thenReturn(Optional.of(userCart));

    cartService.merge(5L, "g1");

    // 상품1: 2+1=3 합산, 상품2 유지 → 2종
    assertThat(userCart.getItems()).hasSize(2);
    CartItem product1 = userCart.getItems().stream()
        .filter(item -> item.getProductId() == 1L)
        .findFirst().orElseThrow();
    assertThat(product1.getQuantity()).isEqualTo(3);
    verify(cartRepository).delete(guestCart);
  }

  @Test
  void mergeIsNoOpWhenNoGuestToken() {
    cartService.merge(5L, null);

    verify(cartRepository, never()).findByGuestToken(any());
  }

  private Product product(Long id, int stock) {
    return Product.builder()
        .id(id)
        .name("상품")
        .price(new BigDecimal("10000.00"))
        .stock(stock)
        .status(ProductStatus.FOR_SALE)
        .build();
  }
}
