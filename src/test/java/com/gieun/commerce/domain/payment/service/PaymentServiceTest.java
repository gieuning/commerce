package com.gieun.commerce.domain.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gieun.commerce.domain.order.entity.Order;
import com.gieun.commerce.domain.order.entity.OrderStatus;
import com.gieun.commerce.domain.order.repository.OrderRepository;
import com.gieun.commerce.domain.order.service.OrderStockService;
import com.gieun.commerce.domain.payment.dto.request.PaymentCancelRequest;
import com.gieun.commerce.domain.payment.dto.request.PaymentConfirmRequest;
import com.gieun.commerce.domain.payment.dto.request.PaymentCreateRequest;
import com.gieun.commerce.domain.payment.dto.response.PaymentDetailResponse;
import com.gieun.commerce.domain.payment.dto.response.PaymentResponse;
import com.gieun.commerce.domain.payment.entity.CompensationStatus;
import com.gieun.commerce.domain.payment.entity.Payment;
import com.gieun.commerce.domain.payment.entity.PaymentCancellation;
import com.gieun.commerce.domain.payment.entity.PaymentCompensation;
import com.gieun.commerce.domain.payment.entity.PaymentEvent;
import com.gieun.commerce.domain.payment.entity.PaymentEventType;
import com.gieun.commerce.domain.payment.entity.PaymentMethod;
import com.gieun.commerce.domain.payment.entity.PaymentReceipt;
import com.gieun.commerce.domain.payment.entity.PaymentStatus;
import com.gieun.commerce.domain.payment.entity.PgProvider;
import com.gieun.commerce.domain.payment.gateway.PaymentCancelCommand;
import com.gieun.commerce.domain.payment.gateway.PaymentConfirmResult;
import com.gieun.commerce.domain.payment.gateway.PaymentGateway;
import com.gieun.commerce.domain.payment.gateway.PaymentGatewayException;
import com.gieun.commerce.domain.payment.repository.PaymentCancellationRepository;
import com.gieun.commerce.domain.payment.repository.PaymentCompensationRepository;
import com.gieun.commerce.domain.payment.repository.PaymentEventRepository;
import com.gieun.commerce.domain.payment.repository.PaymentReceiptRepository;
import com.gieun.commerce.domain.payment.repository.PaymentRepository;
import com.gieun.commerce.global.exception.DomainException;
import com.gieun.commerce.global.exception.DomainExceptionCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @Mock
  PaymentRepository paymentRepository;

  @Mock
  PaymentEventRepository paymentEventRepository;

  @Mock
  PaymentCancellationRepository paymentCancellationRepository;

  @Mock
  PaymentCompensationRepository paymentCompensationRepository;

  @Mock
  PaymentReceiptRepository paymentReceiptRepository;

  @Mock
  OrderRepository orderRepository;

  @Mock
  OrderStockService orderStockService;

  @Mock
  PaymentGateway paymentGateway;

  @Mock
  TransactionTemplate transactionTemplate;

  @InjectMocks
  PaymentService paymentService;

  AtomicBoolean transactionActive = new AtomicBoolean(false);

  @SuppressWarnings("unchecked")
  @org.junit.jupiter.api.BeforeEach
  void setUpTransactionTemplate() {
    org.mockito.Mockito.lenient().when(transactionTemplate.execute(any(TransactionCallback.class))).thenAnswer(invocation -> {
      TransactionCallback<?> callback = invocation.getArgument(0);
      transactionActive.set(true);
      try {
        return callback.doInTransaction(org.mockito.Mockito.mock(TransactionStatus.class));
      } finally {
        transactionActive.set(false);
      }
    });
  }

  @Test
  void requestSavesRequestedPaymentEvent() {
    Long userId = 1L;
    Long orderId = 10L;
    Long paymentId = 100L;
    Order order = Order.builder()
        .id(orderId)
        .userId(userId)
        .status(OrderStatus.CREATED)
        .totalProductPrice(new BigDecimal("30000.00"))
        .discountAmount(BigDecimal.ZERO)
        .shippingFee(BigDecimal.ZERO)
        .totalPrice(new BigDecimal("30000.00"))
        .orderedAt(LocalDateTime.now())
        .build();
    PaymentCreateRequest request = PaymentCreateRequest.builder()
        .orderId(orderId)
        .method(PaymentMethod.CARD)
        .build();

    when(orderRepository.findByIdAndUserIdForUpdate(orderId, userId))
        .thenReturn(Optional.of(order));
    when(paymentRepository.findByOrderIdAndUserIdForUpdate(orderId, userId))
        .thenReturn(Optional.empty());
    when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
      Payment payment = invocation.getArgument(0);
      ReflectionTestUtils.setField(payment, "id", paymentId);
      return payment;
    });

    paymentService.request(userId, request);

    ArgumentCaptor<PaymentEvent> eventCaptor = ArgumentCaptor.forClass(PaymentEvent.class);
    verify(paymentEventRepository).save(eventCaptor.capture());
    PaymentEvent event = eventCaptor.getValue();
    assertThat(event.getPaymentId()).isEqualTo(paymentId);
    assertThat(event.getEventType()).isEqualTo(PaymentEventType.REQUESTED);
    assertThat(event.getPaymentStatus()).isEqualTo(PaymentStatus.REQUESTED);
    assertThat(event.getRequestPayload()).isNull();
    assertThat(event.getResponsePayload()).isNull();
    assertThat(event.getFailureCode()).isNull();
    assertThat(event.getFailureMessage()).isNull();
    assertThat(event.getOccurredAt()).isNotNull();
  }

  @Test
  void requestAllowsRetryAfterFailedPayment() {
    Long userId = 1L;
    Long orderId = 10L;
    Order order = Order.builder()
        .id(orderId)
        .userId(userId)
        .status(OrderStatus.CREATED)
        .totalProductPrice(new BigDecimal("30000.00"))
        .discountAmount(BigDecimal.ZERO)
        .shippingFee(BigDecimal.ZERO)
        .totalPrice(new BigDecimal("30000.00"))
        .orderedAt(LocalDateTime.now())
        .build();
    Payment failedPayment = Payment.request(
        orderId,
        userId,
        "20260628000010FAILED123456",
        PgProvider.TOSS,
        PaymentMethod.CARD,
        new BigDecimal("30000.00")
    );
    failedPayment.fail("PG_ERROR", "승인 실패");
    // 이미 영속된 실패 결제이므로 id가 존재한다. (재요청은 새 INSERT가 아니라 이 row를 재사용)
    ReflectionTestUtils.setField(failedPayment, "id", 101L);
    PaymentCreateRequest request = PaymentCreateRequest.builder()
        .orderId(orderId)
        .method(PaymentMethod.CARD)
        .build();

    when(orderRepository.findByIdAndUserIdForUpdate(orderId, userId))
        .thenReturn(Optional.of(order));
    when(paymentRepository.findByOrderIdAndUserIdForUpdate(orderId, userId))
        .thenReturn(Optional.of(failedPayment));

    PaymentResponse response = paymentService.request(userId, request);

    // FAILED row를 재사용하여 REQUESTED로 리셋 (새 row INSERT 없음)
    assertThat(response.getPaymentId()).isEqualTo(101L);
    assertThat(response.getStatus()).isEqualTo(PaymentStatus.REQUESTED);
    assertThat(failedPayment.getStatus()).isEqualTo(PaymentStatus.REQUESTED);
    assertThat(failedPayment.getFailureCode()).isNull();
    verify(paymentRepository, never()).save(any(Payment.class));
  }

  @Test
  void requestRejectsActivePayment() {
    Long userId = 1L;
    Long orderId = 10L;
    Order order = Order.builder()
        .id(orderId)
        .userId(userId)
        .status(OrderStatus.CREATED)
        .totalProductPrice(new BigDecimal("30000.00"))
        .discountAmount(BigDecimal.ZERO)
        .shippingFee(BigDecimal.ZERO)
        .totalPrice(new BigDecimal("30000.00"))
        .orderedAt(LocalDateTime.now())
        .build();
    Payment requestedPayment = Payment.request(
        orderId,
        userId,
        "20260628000010REQUEST1234",
        PgProvider.TOSS,
        PaymentMethod.CARD,
        new BigDecimal("30000.00")
    );
    PaymentCreateRequest request = PaymentCreateRequest.builder()
        .orderId(orderId)
        .method(PaymentMethod.CARD)
        .build();

    when(orderRepository.findByIdAndUserIdForUpdate(orderId, userId))
        .thenReturn(Optional.of(order));
    when(paymentRepository.findByOrderIdAndUserIdForUpdate(orderId, userId))
        .thenReturn(Optional.of(requestedPayment));

    assertThatThrownBy(() -> paymentService.request(userId, request))
        .isInstanceOfSatisfying(DomainException.class, exception ->
            assertThat(exception.getCode()).isEqualTo(DomainExceptionCode.ALREADY_REQUESTED_PAYMENT.name()));
  }

  @Test
  void getPaymentDetailDoesNotExposeRawPaymentEventPayloads() throws Exception {
    Long userId = 1L;
    Long paymentId = 100L;
    BigDecimal amount = new BigDecimal("30000.00");
    Payment payment = Payment.request(
        10L,
        userId,
        "20260628000010ABCDEF123456",
        PgProvider.TOSS,
        PaymentMethod.CARD,
        amount
    );
    ReflectionTestUtils.setField(payment, "id", paymentId);
    payment.approve("pay_test_key", LocalDateTime.now());
    PaymentEvent event = PaymentEvent.of(PaymentEvent.CreateCommand.builder()
        .paymentId(paymentId)
        .eventType(PaymentEventType.APPROVED)
        .paymentStatus(PaymentStatus.APPROVED)
        .pgProvider(PgProvider.TOSS)
        .requestPayload("{\"paymentKey\":\"pay_test_key\"}")
        .responsePayload("{\"status\":\"DONE\"}")
        .occurredAt(LocalDateTime.now())
        .build());
    PaymentReceipt receipt = PaymentReceipt.issue(PaymentReceipt.IssueCommand.builder()
        .paymentId(paymentId)
        .pgProvider(PgProvider.TOSS)
        .paymentKey("pay_test_key")
        .receiptUrl("https://receipt.example.com")
        .totalAmount(amount)
        .issuedAt(LocalDateTime.now())
        .rawPayload("{\"receipt\":\"ok\"}")
        .build());
    PaymentCancellation cancellation = PaymentCancellation.of(PaymentCancellation.CreateCommand.builder()
        .paymentId(paymentId)
        .pgProvider(PgProvider.TOSS)
        .paymentKey("pay_test_key")
        .pgCancellationKey("cancel_key")
        .cancelAmount(amount)
        .cancelReason("단순 변심")
        .cancelledAt(LocalDateTime.now())
        .requestPayload("{\"cancelReason\":\"단순 변심\"}")
        .responsePayload("{\"status\":\"CANCELED\"}")
        .build());

    when(paymentRepository.findByIdAndUserId(paymentId, userId))
        .thenReturn(Optional.of(payment));
    when(paymentEventRepository.findByPaymentIdOrderByOccurredAtDesc(paymentId))
        .thenReturn(List.of(event));
    when(paymentReceiptRepository.findByPaymentId(paymentId))
        .thenReturn(Optional.of(receipt));
    when(paymentCancellationRepository.findByPaymentIdOrderByCancelledAtDesc(paymentId))
        .thenReturn(List.of(cancellation));

    PaymentDetailResponse response = paymentService.getPaymentDetail(userId, paymentId);

    assertThat(response.getPayment().getPaymentId()).isEqualTo(paymentId);
    assertThat(response.getEvents()).hasSize(1);
    JsonNode eventJson = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .valueToTree(response)
        .path("events")
        .get(0);
    assertThat(eventJson.has("pgEventId")).isFalse();
    assertThat(eventJson.has("requestPayload")).isFalse();
    assertThat(eventJson.has("responsePayload")).isFalse();
    assertThat(eventJson.has("failureCode")).isFalse();
    assertThat(eventJson.has("failureMessage")).isFalse();
    assertThat(eventJson.toString()).doesNotContain("pay_test_key");
    assertThat(response.getReceipt()).isNotNull();
    assertThat(response.getReceipt().getReceiptUrl()).isEqualTo("https://receipt.example.com");
    assertThat(response.getCancellations()).hasSize(1);
    assertThat(response.getCancellations().get(0).getPgCancellationKey()).isEqualTo("cancel_key");
  }

  @Test
  void confirmRejectsMismatchedMerchantOrderIdFromGateway() {
    ConfirmFixture fixture = setUpConfirmFixture();
    BigDecimal chargedAmount = fixture.amount();
    when(paymentGateway.confirm(any())).thenReturn(confirmResult(fixture)
        .merchantOrderId("DIFFERENT_ORDER_ID")
        .requestPayload("{\"orderId\":\"" + fixture.request().getMerchantOrderId() + "\"}")
        .responsePayload("{\"orderId\":\"DIFFERENT_ORDER_ID\"}")
        .build());

    assertThatThrownBy(() -> paymentService.confirm(fixture.userId(), fixture.request()))
        .isInstanceOfSatisfying(DomainException.class, exception ->
            assertThat(exception.getCode()).isEqualTo(DomainExceptionCode.PAYMENT_APPROVAL_FAILED.name()));
    assertFailedConfirmEventSaved(
        fixture,
        DomainExceptionCode.PAYMENT_APPROVAL_FAILED,
        "{\"orderId\":\"" + fixture.request().getMerchantOrderId() + "\"}",
        "{\"orderId\":\"DIFFERENT_ORDER_ID\"}"
    );
    assertCompensatingCancelRequested(fixture.request().getPaymentKey(), chargedAmount);
  }

  @Test
  void confirmRejectsMismatchedPaymentKeyFromGateway() {
    ConfirmFixture fixture = setUpConfirmFixture();
    String chargedPaymentKey = "DIFFERENT_PAYMENT_KEY";
    when(paymentGateway.confirm(any())).thenReturn(confirmResult(fixture)
        .paymentKey(chargedPaymentKey)
        .requestPayload("{\"paymentKey\":\"" + fixture.request().getPaymentKey() + "\"}")
        .responsePayload("{\"paymentKey\":\"DIFFERENT_PAYMENT_KEY\"}")
        .build());

    assertThatThrownBy(() -> paymentService.confirm(fixture.userId(), fixture.request()))
        .isInstanceOfSatisfying(DomainException.class, exception ->
            assertThat(exception.getCode()).isEqualTo(DomainExceptionCode.INVALID_PAYMENT_KEY.name()));
    assertFailedConfirmEventSaved(
        fixture,
        DomainExceptionCode.INVALID_PAYMENT_KEY,
        "{\"paymentKey\":\"" + fixture.request().getPaymentKey() + "\"}",
        "{\"paymentKey\":\"DIFFERENT_PAYMENT_KEY\"}"
    );
    assertCompensatingCancelRequested(chargedPaymentKey, fixture.amount());
  }

  @Test
  void confirmRejectsMismatchedTotalAmountFromGateway() {
    ConfirmFixture fixture = setUpConfirmFixture();
    BigDecimal chargedAmount = new BigDecimal("31000.00");
    when(paymentGateway.confirm(any())).thenReturn(confirmResult(fixture)
        .totalAmount(chargedAmount)
        .requestPayload("{\"amount\":30000.00}")
        .responsePayload("{\"totalAmount\":31000.00}")
        .build());

    assertThatThrownBy(() -> paymentService.confirm(fixture.userId(), fixture.request()))
        .isInstanceOfSatisfying(DomainException.class, exception ->
            assertThat(exception.getCode()).isEqualTo(DomainExceptionCode.PAYMENT_AMOUNT_MISMATCH.name()));
    assertFailedConfirmEventSaved(
        fixture,
        DomainExceptionCode.PAYMENT_AMOUNT_MISMATCH,
        "{\"amount\":30000.00}",
        "{\"totalAmount\":31000.00}"
    );
    assertCompensatingCancelRequested(fixture.request().getPaymentKey(), chargedAmount);
  }

  @Test
  void confirmSavesFailedEventWhenPgStatusIsNotDone() {
    ConfirmFixture fixture = setUpConfirmFixture();
    when(paymentGateway.confirm(any())).thenReturn(confirmResult(fixture)
        .pgStatus("WAITING_FOR_DEPOSIT")
        .requestPayload("{\"status\":\"request\"}")
        .responsePayload("{\"status\":\"WAITING_FOR_DEPOSIT\"}")
        .build());

    assertThatThrownBy(() -> paymentService.confirm(fixture.userId(), fixture.request()))
        .isInstanceOfSatisfying(DomainException.class, exception ->
            assertThat(exception.getCode()).isEqualTo(DomainExceptionCode.PAYMENT_APPROVAL_FAILED.name()));
    assertFailedConfirmEventSaved(
        fixture,
        DomainExceptionCode.PAYMENT_APPROVAL_FAILED,
        "{\"status\":\"request\"}",
        "{\"status\":\"WAITING_FOR_DEPOSIT\"}"
    );
  }

  @Test
  void confirmCallsGatewayOutsideLocalTransaction() {
    ConfirmFixture fixture = setUpConfirmFixture();
    when(paymentGateway.confirm(any())).thenAnswer(invocation -> {
      assertThat(transactionActive).isFalse();
      return confirmResult(fixture).build();
    });

    paymentService.confirm(fixture.userId(), fixture.request());

    assertThat(fixture.payment().getStatus()).isEqualTo(PaymentStatus.APPROVED);
    verify(transactionTemplate, org.mockito.Mockito.times(2)).execute(any(TransactionCallback.class));
  }

  @Test
  void confirmCommitsFailedPaymentAndEventBeforeThrowingGatewayFailure() {
    ConfirmFixture fixture = setUpConfirmFixture();
    when(paymentGateway.confirm(any())).thenThrow(new PaymentGatewayException(
        DomainExceptionCode.PAYMENT_APPROVAL_FAILED,
        "PG_ERROR",
        "승인 실패",
        "{\"code\":\"PG_ERROR\"}"
    ));

    assertThatThrownBy(() -> paymentService.confirm(fixture.userId(), fixture.request()))
        .isInstanceOfSatisfying(DomainException.class, exception ->
            assertThat(exception.getCode()).isEqualTo(DomainExceptionCode.PAYMENT_APPROVAL_FAILED.name()));

    assertThat(fixture.payment().getStatus()).isEqualTo(PaymentStatus.FAILED);
    assertThat(fixture.payment().getFailureCode()).isEqualTo("PG_ERROR");
    assertThat(fixture.payment().getFailureMessage()).isEqualTo("승인 실패");

    ArgumentCaptor<PaymentEvent> eventCaptor = ArgumentCaptor.forClass(PaymentEvent.class);
    verify(paymentEventRepository).save(eventCaptor.capture());
    PaymentEvent event = eventCaptor.getValue();
    assertThat(event.getPaymentId()).isEqualTo(fixture.payment().getId());
    assertThat(event.getEventType()).isEqualTo(PaymentEventType.FAILED);
    assertThat(event.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
    assertThat(event.getResponsePayload()).isEqualTo("{\"code\":\"PG_ERROR\"}");
    assertThat(event.getFailureCode()).isEqualTo("PG_ERROR");
    assertThat(event.getFailureMessage()).isEqualTo("승인 실패");
    verify(transactionTemplate, org.mockito.Mockito.atLeastOnce()).execute(any(TransactionCallback.class));
  }

  @Test
  void cancelCommitsFailedEventBeforeThrowingGatewayFailure() {
    Long userId = 1L;
    Long orderId = 10L;
    Long paymentId = 100L;
    BigDecimal amount = new BigDecimal("30000.00");
    Order order = Order.builder()
        .id(orderId)
        .userId(userId)
        .status(OrderStatus.PAID)
        .totalProductPrice(amount)
        .discountAmount(BigDecimal.ZERO)
        .shippingFee(BigDecimal.ZERO)
        .totalPrice(amount)
        .orderedAt(LocalDateTime.now())
        .build();
    Payment payment = Payment.request(
        orderId,
        userId,
        "20260628000010ABCDEF123456",
        PgProvider.TOSS,
        PaymentMethod.CARD,
        amount
    );
    ReflectionTestUtils.setField(payment, "id", paymentId);
    payment.approve("pay_test_key", LocalDateTime.now());
    PaymentCancelRequest request = PaymentCancelRequest.builder()
        .cancelAmount(amount)
        .cancelReason("단순 변심")
        .build();

    when(paymentRepository.findByIdAndUserId(paymentId, userId))
        .thenReturn(Optional.of(payment));
    when(orderRepository.findByIdAndUserIdForUpdate(orderId, userId))
        .thenReturn(Optional.of(order));
    when(paymentRepository.findByIdAndUserIdForUpdate(paymentId, userId))
        .thenReturn(Optional.of(payment));
    when(paymentGateway.cancel(any())).thenThrow(new PaymentGatewayException(
        DomainExceptionCode.PAYMENT_CANCEL_FAILED,
        "PG_CANCEL_ERROR",
        "취소 실패",
        "{\"code\":\"PG_CANCEL_ERROR\"}"
    ));

    assertThatThrownBy(() -> paymentService.cancel(userId, paymentId, request))
        .isInstanceOfSatisfying(DomainException.class, exception ->
            assertThat(exception.getCode()).isEqualTo(DomainExceptionCode.PAYMENT_CANCEL_FAILED.name()));

    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);

    ArgumentCaptor<PaymentEvent> eventCaptor = ArgumentCaptor.forClass(PaymentEvent.class);
    verify(paymentEventRepository).save(eventCaptor.capture());
    PaymentEvent event = eventCaptor.getValue();
    assertThat(event.getPaymentId()).isEqualTo(paymentId);
    assertThat(event.getEventType()).isEqualTo(PaymentEventType.FAILED);
    assertThat(event.getPaymentStatus()).isEqualTo(PaymentStatus.APPROVED);
    assertThat(event.getResponsePayload()).isEqualTo("{\"code\":\"PG_CANCEL_ERROR\"}");
    assertThat(event.getFailureCode()).isEqualTo("PG_CANCEL_ERROR");
    assertThat(event.getFailureMessage()).isEqualTo("취소 실패");
    verify(transactionTemplate, org.mockito.Mockito.atLeastOnce()).execute(any(TransactionCallback.class));
  }

  @Test
  void cancelCallsGatewayOutsideLocalTransaction() {
    Long userId = 1L;
    Long orderId = 10L;
    Long paymentId = 100L;
    BigDecimal amount = new BigDecimal("30000.00");
    Order order = Order.builder()
        .id(orderId)
        .userId(userId)
        .status(OrderStatus.PAID)
        .totalProductPrice(amount)
        .discountAmount(BigDecimal.ZERO)
        .shippingFee(BigDecimal.ZERO)
        .totalPrice(amount)
        .orderedAt(LocalDateTime.now())
        .build();
    Payment payment = Payment.request(
        orderId,
        userId,
        "20260628000010ABCDEF123456",
        PgProvider.TOSS,
        PaymentMethod.CARD,
        amount
    );
    ReflectionTestUtils.setField(payment, "id", paymentId);
    payment.approve("pay_test_key", LocalDateTime.now());
    PaymentCancelRequest request = PaymentCancelRequest.builder()
        .cancelAmount(amount)
        .cancelReason("단순 변심")
        .build();

    when(paymentRepository.findByIdAndUserId(paymentId, userId))
        .thenReturn(Optional.of(payment));
    when(orderRepository.findByIdAndUserIdForUpdate(orderId, userId))
        .thenReturn(Optional.of(order));
    when(paymentRepository.findByIdAndUserIdForUpdate(paymentId, userId))
        .thenReturn(Optional.of(payment));
    when(paymentGateway.cancel(any())).thenAnswer(invocation -> {
      assertThat(transactionActive).isFalse();
      return com.gieun.commerce.domain.payment.gateway.PaymentCancelResult.builder()
          .paymentKey("pay_test_key")
          .pgStatus("CANCELED")
          .pgCancellationKey("cancel_key")
          .cancelAmount(amount)
          .cancelledAt(OffsetDateTime.now())
          .build();
    });

    paymentService.cancel(userId, paymentId, request);

    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
    verify(transactionTemplate, org.mockito.Mockito.times(2)).execute(any(TransactionCallback.class));
  }

  

  @Test
  void confirmCompensatesAndRecordsDoneWhenStockRunsOut() {
    ConfirmFixture fixture = setUpConfirmFixture();
    when(paymentGateway.confirm(any())).thenReturn(confirmResult(fixture).build());

    // stage 2 재고 차감에서 품절 → failure 리턴 경로로 진입
    org.mockito.Mockito.doThrow(new DomainException(DomainExceptionCode.OUT_OF_STOCK_PRODUCT))
        .when(orderStockService).decrease(any());
    // 보상 환불(cancel)은 성공: 기본 mock이 예외 없이 반환 → 성공으로 간주됨

    assertThatThrownBy(() -> paymentService.confirm(fixture.userId(), fixture.request()))
        .isInstanceOfSatisfying(DomainException.class, e ->
            assertThat(e.getCode()).isEqualTo(DomainExceptionCode.OUT_OF_STOCK_PRODUCT.name()));

    // 보상 환불이 실제로 시도됐는가
    assertCompensatingCancelRequested(fixture.request().getPaymentKey(), fixture.amount());

    // 아웃박스에 DONE으로 기록됐는가
    ArgumentCaptor<PaymentCompensation> captor = ArgumentCaptor.forClass(PaymentCompensation.class);
    verify(paymentCompensationRepository).save(captor.capture());
    PaymentCompensation compensation = captor.getValue();
    assertThat(compensation.getStatus()).isEqualTo(CompensationStatus.DONE);
    assertThat(compensation.getAttemptCount()).isEqualTo(1);
    assertThat(compensation.getNextRetryAt()).isNull();

    // 결제는 FAILED로 마감
    assertThat(fixture.payment().getStatus()).isEqualTo(PaymentStatus.FAILED);
  }

  @Test
  void confirmRecordsPendingCompensationWhenRefundFails() {
    ConfirmFixture fixture = setUpConfirmFixture();
    when(paymentGateway.confirm(any())).thenReturn(confirmResult(fixture).build());
    org.mockito.Mockito.doThrow(new DomainException(DomainExceptionCode.OUT_OF_STOCK_PRODUCT))
        .when(orderStockService).decrease(any());
    // 보상 환불(cancel)마저 실패 → PENDING으로 남겨 스케줄러가 재시도해야 함
    when(paymentGateway.cancel(any())).thenThrow(new PaymentGatewayException(
        DomainExceptionCode.PAYMENT_CANCEL_FAILED, "PG_CANCEL_ERR", "취소 실패",
        "{\"code\":\"PG_CANCEL_ERR\"}"));

    assertThatThrownBy(() -> paymentService.confirm(fixture.userId(), fixture.request()))
        .isInstanceOfSatisfying(DomainException.class, e ->
            assertThat(e.getCode()).isEqualTo(DomainExceptionCode.OUT_OF_STOCK_PRODUCT.name()));

    ArgumentCaptor<PaymentCompensation> captor = ArgumentCaptor.forClass(PaymentCompensation.class);
    verify(paymentCompensationRepository).save(captor.capture());
    PaymentCompensation compensation = captor.getValue();
    assertThat(compensation.getStatus()).isEqualTo(CompensationStatus.PENDING);
    assertThat(compensation.getAttemptCount()).isEqualTo(1);
    assertThat(compensation.getNextRetryAt()).isNotNull();
    assertThat(compensation.getLastError()).contains("PG_CANCEL_ERR");

    assertThat(fixture.payment().getStatus()).isEqualTo(PaymentStatus.FAILED);
  }

  private ConfirmFixture setUpConfirmFixture() {
    Long userId = 1L;
    Long orderId = 10L;
    Long paymentId = 100L;
    String merchantOrderId = "20260628000010ABCDEF123456";
    String paymentKey = "pay_test_key";
    BigDecimal amount = new BigDecimal("30000.00");
    Order order = Order.builder()
        .id(orderId)
        .userId(userId)
        .status(OrderStatus.CREATED)
        .totalProductPrice(amount)
        .discountAmount(BigDecimal.ZERO)
        .shippingFee(BigDecimal.ZERO)
        .totalPrice(amount)
        .orderedAt(LocalDateTime.now())
        .build();
    Payment payment = Payment.request(
        orderId,
        userId,
        merchantOrderId,
        PgProvider.TOSS,
        PaymentMethod.CARD,
        amount
    );
    ReflectionTestUtils.setField(payment, "id", paymentId);
    PaymentConfirmRequest request = PaymentConfirmRequest.builder()
        .merchantOrderId(merchantOrderId)
        .paymentKey(paymentKey)
        .amount(amount)
        .build();

    when(paymentRepository.findByMerchantOrderIdAndUserId(merchantOrderId, userId))
        .thenReturn(Optional.of(payment));
    when(orderRepository.findByIdAndUserIdForUpdate(orderId, userId))
        .thenReturn(Optional.of(order));
    when(paymentRepository.findByIdAndUserIdForUpdate(paymentId, userId))
        .thenReturn(Optional.of(payment));

    return new ConfirmFixture(userId, request, amount, payment);
  }

  private PaymentConfirmResult.PaymentConfirmResultBuilder confirmResult(ConfirmFixture fixture) {
    return PaymentConfirmResult.builder()
        .merchantOrderId(fixture.request().getMerchantOrderId())
        .paymentKey(fixture.request().getPaymentKey())
        .pgStatus("DONE")
        .totalAmount(fixture.amount())
        .approvedAt(OffsetDateTime.now());
  }

  private void assertFailedConfirmEventSaved(
      ConfirmFixture fixture,
      DomainExceptionCode exceptionCode,
      String requestPayload,
      String responsePayload
  ) {
    assertThat(fixture.payment().getStatus()).isEqualTo(PaymentStatus.FAILED);
    assertThat(fixture.payment().getFailureCode()).isEqualTo(exceptionCode.name());
    assertThat(fixture.payment().getFailureMessage()).isEqualTo(exceptionCode.getMessage());

    ArgumentCaptor<PaymentEvent> eventCaptor = ArgumentCaptor.forClass(PaymentEvent.class);
    verify(paymentEventRepository).save(eventCaptor.capture());
    PaymentEvent event = eventCaptor.getValue();
    assertThat(event.getPaymentId()).isEqualTo(fixture.payment().getId());
    assertThat(event.getEventType()).isEqualTo(PaymentEventType.FAILED);
    assertThat(event.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
    assertThat(event.getRequestPayload()).isEqualTo(requestPayload);
    assertThat(event.getResponsePayload()).isEqualTo(responsePayload);
    assertThat(event.getFailureCode()).isEqualTo(exceptionCode.name());
    assertThat(event.getFailureMessage()).isEqualTo(exceptionCode.getMessage());
    verify(transactionTemplate, org.mockito.Mockito.atLeastOnce()).execute(any(TransactionCallback.class));
  }

  private void assertCompensatingCancelRequested(String paymentKey, BigDecimal amount) {
    ArgumentCaptor<PaymentCancelCommand> commandCaptor = ArgumentCaptor.forClass(PaymentCancelCommand.class);
    verify(paymentGateway).cancel(commandCaptor.capture());
    PaymentCancelCommand command = commandCaptor.getValue();
    assertThat(command.getPaymentKey()).isEqualTo(paymentKey);
    assertThat(command.getCancelAmount()).isEqualByComparingTo(amount);
    assertThat(command.getCancelReason()).isEqualTo("로컬 결제 승인 검증 실패");
  }

  private record ConfirmFixture(
      Long userId,
      PaymentConfirmRequest request,
      BigDecimal amount,
      Payment payment
  ) {
  }
}
