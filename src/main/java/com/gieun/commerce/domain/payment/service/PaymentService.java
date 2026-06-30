package com.gieun.commerce.domain.payment.service;

import com.gieun.commerce.domain.order.entity.Order;
import com.gieun.commerce.domain.order.entity.OrderStatus;
import com.gieun.commerce.domain.order.repository.OrderRepository;
import com.gieun.commerce.domain.order.service.OrderStockService;
import com.gieun.commerce.domain.payment.dto.request.PaymentCancelRequest;
import com.gieun.commerce.domain.payment.dto.request.PaymentConfirmRequest;
import com.gieun.commerce.domain.payment.dto.request.PaymentCreateRequest;
import com.gieun.commerce.domain.payment.dto.response.PaymentDetailResponse;
import com.gieun.commerce.domain.payment.dto.response.PaymentResponse;
import com.gieun.commerce.domain.payment.entity.Payment;
import com.gieun.commerce.domain.payment.entity.PaymentCancellation;
import com.gieun.commerce.domain.payment.entity.PaymentEvent;
import com.gieun.commerce.domain.payment.entity.PaymentEvent.CreateCommand;
import com.gieun.commerce.domain.payment.entity.PaymentEventType;
import com.gieun.commerce.domain.payment.entity.PaymentReceipt;
import com.gieun.commerce.domain.payment.entity.PaymentStatus;
import com.gieun.commerce.domain.payment.entity.PgProvider;
import com.gieun.commerce.domain.payment.gateway.PaymentCancelCommand;
import com.gieun.commerce.domain.payment.gateway.PaymentCancelResult;
import com.gieun.commerce.domain.payment.gateway.PaymentConfirmCommand;
import com.gieun.commerce.domain.payment.gateway.PaymentConfirmResult;
import com.gieun.commerce.domain.payment.gateway.PaymentGateway;
import com.gieun.commerce.domain.payment.gateway.PaymentGatewayException;
import com.gieun.commerce.domain.payment.repository.PaymentCancellationRepository;
import com.gieun.commerce.domain.payment.repository.PaymentEventRepository;
import com.gieun.commerce.domain.payment.repository.PaymentRepository;
import com.gieun.commerce.domain.payment.repository.PaymentReceiptRepository;
import com.gieun.commerce.global.exception.DomainException;
import com.gieun.commerce.global.exception.DomainExceptionCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class PaymentService {

  private static final DateTimeFormatter MERCHANT_ORDER_DATE_FORMAT =
      DateTimeFormatter.BASIC_ISO_DATE;
  private static final int MERCHANT_ORDER_UUID_LENGTH = 12;
  private static final String COMPENSATING_CANCEL_REASON = "로컬 결제 승인 검증 실패";

  private final PaymentRepository paymentRepository;
  private final PaymentEventRepository paymentEventRepository;
  private final PaymentCancellationRepository paymentCancellationRepository;
  private final PaymentReceiptRepository paymentReceiptRepository;
  private final OrderRepository orderRepository;
  private final OrderStockService orderStockService;
  private final PaymentGateway paymentGateway;
  private final TransactionTemplate transactionTemplate;


  @Transactional
  public PaymentResponse request(Long userId, PaymentCreateRequest request) {
    Order order = orderRepository.findByIdAndUserIdForUpdate(request.getOrderId(), userId)
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_ORDER));

    if (order.getStatus() != OrderStatus.CREATED) {
      throw new DomainException(DomainExceptionCode.CANNOT_REQUEST_PAYMENT);
    }

    Payment payment = paymentRepository.findByOrderIdAndUserIdForUpdate(order.getId(), userId)
        .map(existing -> {
          if (existing.getStatus() != PaymentStatus.FAILED) {
            throw new DomainException(DomainExceptionCode.ALREADY_REQUESTED_PAYMENT);
          }
          existing.retry(
              createMerchantOrderId(order.getId()),
              request.getMethod(),
              order.getTotalPrice()
          );
          return existing;
        })
        .orElseGet(() -> paymentRepository.save(Payment.request(
            order.getId(),
            userId,
            createMerchantOrderId(order.getId()),
            PgProvider.TOSS,
            request.getMethod(),
            order.getTotalPrice()
        )));

    savePaymentEvent(
        payment,
        PaymentEventType.REQUESTED,
        payment.getStatus(),
        PaymentEventPayload.empty(),
        LocalDateTime.now()
    );
    return PaymentResponse.of(payment);
  }

  @Transactional(readOnly = true)
  public PaymentResponse getPayment(Long userId, Long paymentId) {
    Payment payment = paymentRepository.findByIdAndUserId(paymentId, userId)
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_PAYMENT));

    return PaymentResponse.of(payment);
  }

  @Transactional(readOnly = true)
  public PaymentDetailResponse getPaymentDetail(Long userId, Long paymentId) {
    Payment payment = paymentRepository.findByIdAndUserId(paymentId, userId)
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_PAYMENT));

    return PaymentDetailResponse.of(
        payment,
        paymentReceiptRepository.findByPaymentId(payment.getId()).orElse(null),
        paymentEventRepository.findByPaymentIdOrderByOccurredAtDesc(payment.getId()),
        paymentCancellationRepository.findByPaymentIdOrderByCancelledAtDesc(payment.getId())
    );
  }

  public PaymentResponse confirm(Long userId, PaymentConfirmRequest request) {
    ConfirmContext context = Objects.requireNonNull(
        transactionTemplate.execute(status -> prepareConfirm(userId, request))
    );

    PaymentConfirmResult confirmResult;
    try {
      confirmResult = paymentGateway.confirm(
          PaymentConfirmCommand.builder()
              .paymentKey(request.getPaymentKey())
              .merchantOrderId(request.getMerchantOrderId())
              .amount(request.getAmount())
              .build()
      );
    } catch (PaymentGatewayException exception) {
      PaymentTransactionResult result = Objects.requireNonNull(
          transactionTemplate.execute(status -> persistConfirmGatewayFailure(context, exception))
      );
      throw new DomainException(result.exceptionCode());
    }

    PaymentTransactionResult result = Objects.requireNonNull(
        transactionTemplate.execute(status -> persistConfirmResult(userId, request, confirmResult))
    );

    if (result.exceptionCode() != null) {
      PaymentGatewayException compensationException = compensateConfirmedPayment(confirmResult);
      transactionTemplate.execute(status ->
          persistConfirmValidationFailure(context, confirmResult, result.exceptionCode(), compensationException));
      throw new DomainException(result.exceptionCode());
    }

    return result.response();
  }

  private ConfirmContext prepareConfirm(Long userId, PaymentConfirmRequest request) {
    Payment paymentSnapshot = paymentRepository
        .findByMerchantOrderIdAndUserId(request.getMerchantOrderId(), userId)
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_PAYMENT));

    Order order = orderRepository.findByIdAndUserIdForUpdate(paymentSnapshot.getOrderId(), userId)
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_ORDER));

    Payment payment = paymentRepository.findByIdAndUserIdForUpdate(paymentSnapshot.getId(), userId)
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_PAYMENT));

    validateConfirmable(order, payment, request);

    return new ConfirmContext(userId, payment.getId());
  }

  private PaymentTransactionResult persistConfirmResult(
      Long userId,
      PaymentConfirmRequest request,
      PaymentConfirmResult result
  ) {
    Payment paymentSnapshot = paymentRepository
        .findByMerchantOrderIdAndUserId(request.getMerchantOrderId(), userId)
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_PAYMENT));

    Order order = orderRepository.findByIdAndUserIdForUpdate(paymentSnapshot.getOrderId(), userId)
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_ORDER));

    Payment payment = paymentRepository.findByIdAndUserIdForUpdate(paymentSnapshot.getId(), userId)
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_PAYMENT));

    try {
      validateConfirmable(order, payment, request);
      validateConfirmResult(payment, request, result);
    } catch (DomainException exception) {
      return PaymentTransactionResult.failure(DomainExceptionCode.valueOf(exception.getCode()));
    }

    approvePayment(order, payment, result);
    return PaymentTransactionResult.success(PaymentResponse.of(payment));
  }

  private PaymentTransactionResult persistConfirmGatewayFailure(
      ConfirmContext context,
      PaymentGatewayException exception
  ) {
    Payment payment = paymentRepository.findByIdAndUserIdForUpdate(context.paymentId(), context.userId())
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_PAYMENT));

    failPayment(payment, exception);
    return PaymentTransactionResult.failure(exception.getExceptionCode());
  }

  private PaymentTransactionResult persistConfirmValidationFailure(
      ConfirmContext context,
      PaymentConfirmResult result,
      DomainExceptionCode exceptionCode,
      PaymentGatewayException compensationException
  ) {
    Payment payment = paymentRepository.findByIdAndUserIdForUpdate(context.paymentId(), context.userId())
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_PAYMENT));

    if (compensationException != null) {
      savePaymentEvent(
          payment,
          PaymentEventType.FAILED,
          payment.getStatus(),
          PaymentEventPayload.failure(
              null,
              compensationException.getResponsePayload(),
              compensationException.getPgCode(),
              compensationException.getPgMessage()
          ),
          LocalDateTime.now()
      );
    }

    failPayment(payment, result, exceptionCode);
    return PaymentTransactionResult.failure(exceptionCode);
  }

  public PaymentResponse cancel(Long userId, Long paymentId, PaymentCancelRequest request) {
    CancelContext context = Objects.requireNonNull(
        transactionTemplate.execute(status -> prepareCancel(userId, paymentId, request))
    );

    PaymentCancelResult cancelResult;
    try {
      cancelResult = paymentGateway.cancel(
          PaymentCancelCommand.builder()
              .paymentKey(context.paymentKey())
              .cancelAmount(context.cancelAmount())
              .cancelReason(request.getCancelReason())
              .build()
      );
    } catch (PaymentGatewayException exception) {
      PaymentTransactionResult result = Objects.requireNonNull(
          transactionTemplate.execute(status -> persistCancelGatewayFailure(context, exception))
      );
      throw new DomainException(result.exceptionCode());
    }

    PaymentTransactionResult result = Objects.requireNonNull(
        transactionTemplate.execute(status -> persistCancelResult(userId, paymentId, request, cancelResult))
    );

    if (result.exceptionCode() != null) {
      throw new DomainException(result.exceptionCode());
    }

    return result.response();
  }

  private CancelContext prepareCancel(
      Long userId,
      Long paymentId,
      PaymentCancelRequest request
  ) {
    Payment paymentSnapshot = paymentRepository.findByIdAndUserId(paymentId, userId)
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_PAYMENT));

    Order order = orderRepository.findByIdAndUserIdForUpdate(paymentSnapshot.getOrderId(), userId)
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_ORDER));

    Payment payment = paymentRepository.findByIdAndUserIdForUpdate(paymentId, userId)
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_PAYMENT));

    validateCancelable(order, payment);

    BigDecimal cancelAmount = resolveFullCancelAmount(request, payment);

    return new CancelContext(userId, payment.getId(), payment.getPaymentKey(), cancelAmount);
  }

  private PaymentTransactionResult persistCancelResult(
      Long userId,
      Long paymentId,
      PaymentCancelRequest request,
      PaymentCancelResult result
  ) {
    Payment paymentSnapshot = paymentRepository.findByIdAndUserId(paymentId, userId)
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_PAYMENT));

    Order order = orderRepository.findByIdAndUserIdForUpdate(paymentSnapshot.getOrderId(), userId)
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_ORDER));

    Payment payment = paymentRepository.findByIdAndUserIdForUpdate(paymentId, userId)
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_PAYMENT));

    validateCancelable(order, payment);
    resolveFullCancelAmount(request, payment);
    cancelPayment(order, payment, request, result);

    return PaymentTransactionResult.success(PaymentResponse.of(payment));
  }

  private PaymentTransactionResult persistCancelGatewayFailure(
      CancelContext context,
      PaymentGatewayException exception
  ) {
    Payment payment = paymentRepository.findByIdAndUserIdForUpdate(context.paymentId(), context.userId())
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_PAYMENT));

    savePaymentEvent(
        payment,
        PaymentEventType.FAILED,
        payment.getStatus(),
        PaymentEventPayload.failure(
            null,
            exception.getResponsePayload(),
            exception.getPgCode(),
            exception.getPgMessage()
        ),
        LocalDateTime.now()
    );
    return PaymentTransactionResult.failure(exception.getExceptionCode());
  }

  private void validateConfirmable(
      Order order,
      Payment payment,
      PaymentConfirmRequest request
  ) {
    if (!payment.getOrderId().equals(order.getId())) {
      throw new DomainException(DomainExceptionCode.NOT_FOUND_PAYMENT);
    }

    if (payment.getStatus() != PaymentStatus.REQUESTED) {
      throw new DomainException(DomainExceptionCode.CANNOT_CONFIRM_PAYMENT);
    }

    if (order.getStatus() != OrderStatus.CREATED) {
      throw new DomainException(DomainExceptionCode.CANNOT_CONFIRM_PAYMENT);
    }

    if (payment.getPaymentKey() != null
        && !Objects.equals(payment.getPaymentKey(), request.getPaymentKey())) {
      throw new DomainException(DomainExceptionCode.INVALID_PAYMENT_KEY);
    }

    if (payment.getAmount().compareTo(request.getAmount()) != 0
        || order.getTotalPrice().compareTo(request.getAmount()) != 0) {
      throw new DomainException(DomainExceptionCode.PAYMENT_AMOUNT_MISMATCH);
    }
  }

  private void validateConfirmResult(
      Payment payment,
      PaymentConfirmRequest request,
      PaymentConfirmResult result
  ) {
    if (!"DONE".equals(result.getPgStatus())) {
      throw new DomainException(DomainExceptionCode.PAYMENT_APPROVAL_FAILED);
    }

    if (!Objects.equals(result.getMerchantOrderId(), request.getMerchantOrderId())) {
      throw new DomainException(DomainExceptionCode.PAYMENT_APPROVAL_FAILED);
    }

    if (!Objects.equals(result.getPaymentKey(), request.getPaymentKey())) {
      throw new DomainException(DomainExceptionCode.INVALID_PAYMENT_KEY);
    }

    if (result.getTotalAmount() == null
        || result.getTotalAmount().compareTo(payment.getAmount()) != 0) {
      throw new DomainException(DomainExceptionCode.PAYMENT_AMOUNT_MISMATCH);
    }
  }

  private void approvePayment(Order order, Payment payment, PaymentConfirmResult result) {
    LocalDateTime approvedAt = toLocalDateTimeOrNow(result.getApprovedAt());

    payment.approve(result.getPaymentKey(), approvedAt);
    order.pay();

    saveReceipt(payment, result);
    savePaymentEvent(
        payment,
        PaymentEventType.APPROVED,
        payment.getStatus(),
        PaymentEventPayload.success(result.getRequestPayload(), result.getResponsePayload()),
        approvedAt
    );
  }

  private void failPayment(
      Payment payment,
      PaymentConfirmResult result,
      DomainExceptionCode exceptionCode
  ) {
    payment.fail(exceptionCode.name(), exceptionCode.getMessage());
    savePaymentEvent(
        payment,
        PaymentEventType.FAILED,
        payment.getStatus(),
        PaymentEventPayload.failure(
            result.getRequestPayload(),
            result.getResponsePayload(),
            exceptionCode.name(),
            exceptionCode.getMessage()
        ),
        LocalDateTime.now()
    );
  }

  private void failPayment(Payment payment, PaymentGatewayException exception) {
    payment.fail(exception.getPgCode(), exception.getPgMessage());
    savePaymentEvent(
        payment,
        PaymentEventType.FAILED,
        payment.getStatus(),
        PaymentEventPayload.failure(
            null,
            exception.getResponsePayload(),
            exception.getPgCode(),
            exception.getPgMessage()
        ),
        LocalDateTime.now()
    );
  }

  private void validateCancelable(Order order, Payment payment) {
    if (!payment.getOrderId().equals(order.getId())) {
      throw new DomainException(DomainExceptionCode.NOT_FOUND_PAYMENT);
    }

    if (payment.getStatus() != PaymentStatus.APPROVED) {
      throw new DomainException(DomainExceptionCode.CANNOT_CANCEL_PAYMENT);
    }

    if (order.getStatus() != OrderStatus.PAID) {
      throw new DomainException(DomainExceptionCode.CANNOT_CANCEL_ORDER);
    }
  }

  private PaymentGatewayException compensateConfirmedPayment(PaymentConfirmResult result) {
    if (!"DONE".equals(result.getPgStatus())
        || result.getPaymentKey() == null
        || result.getPaymentKey().isBlank()
        || result.getTotalAmount() == null) {
      return null;
    }

    try {
      paymentGateway.cancel(
          PaymentCancelCommand.builder()
              .paymentKey(result.getPaymentKey())
              .cancelAmount(result.getTotalAmount())
              .cancelReason(COMPENSATING_CANCEL_REASON)
              .build()
      );
      return null;
    } catch (PaymentGatewayException exception) {
      return exception;
    }
  }

  private void saveReceipt(Payment payment, PaymentConfirmResult result) {
    if (result.getReceiptUrl() == null || result.getReceiptUrl().isBlank()) {
      return;
    }

    PaymentReceipt receipt = PaymentReceipt.issue(
        PaymentReceipt.IssueCommand.builder()
            .paymentId(payment.getId())
            .pgProvider(payment.getPgProvider())
            .paymentKey(payment.getPaymentKey())
            .receiptUrl(result.getReceiptUrl())
            .totalAmount(result.getTotalAmount())
            .suppliedAmount(result.getSuppliedAmount())
            .vat(result.getVat())
            .issuedAt(toLocalDateTime(result.getApprovedAt()))
            .rawPayload(result.getResponsePayload())
            .build()
    );
    paymentReceiptRepository.save(receipt);
  }

  private void cancelPayment(
      Order order,
      Payment payment,
      PaymentCancelRequest request,
      PaymentCancelResult result
  ) {
    LocalDateTime cancelledAt = toLocalDateTimeOrNow(result.getCancelledAt());

    payment.cancel(cancelledAt);
    order.cancelPaid();
    orderStockService.restore(order);

    PaymentCancellation cancellation = PaymentCancellation.of(
        PaymentCancellation.CreateCommand.builder()
            .paymentId(payment.getId())
            .pgProvider(payment.getPgProvider())
            .paymentKey(payment.getPaymentKey())
            .pgCancellationKey(result.getPgCancellationKey())
            .cancelAmount(result.getCancelAmount() == null ? payment.getAmount() : result.getCancelAmount())
            .cancelReason(request.getCancelReason())
            .cancelledAt(cancelledAt)
            .requestPayload(result.getRequestPayload())
            .responsePayload(result.getResponsePayload())
            .build()
    );
    paymentCancellationRepository.save(cancellation);

    savePaymentEvent(
        payment,
        PaymentEventType.CANCELLED,
        payment.getStatus(),
        PaymentEventPayload.success(result.getRequestPayload(), result.getResponsePayload()),
        cancelledAt
    );
  }

  private String createMerchantOrderId(Long orderId) {
    String date = LocalDate.now().format(MERCHANT_ORDER_DATE_FORMAT);
    String paddedOrderId = String.format("%06d", orderId);
    String uuid = UUID.randomUUID()
        .toString()
        .replace("-", "")
        .substring(0, MERCHANT_ORDER_UUID_LENGTH)
        .toUpperCase();

    return date + paddedOrderId + uuid;
  }

  private LocalDateTime toLocalDateTimeOrNow(OffsetDateTime dateTime) {
    LocalDateTime converted = toLocalDateTime(dateTime);
    return converted == null ? LocalDateTime.now() : converted;
  }

  private LocalDateTime toLocalDateTime(OffsetDateTime dateTime) {
    return dateTime == null ? null : dateTime.toLocalDateTime();
  }

  private BigDecimal resolveFullCancelAmount(PaymentCancelRequest request, Payment payment) {
    if (request.getCancelAmount() == null) {
      return payment.getAmount(); // 전체 취소
    }

    if (request.getCancelAmount().compareTo(payment.getAmount()) != 0) {
      throw new DomainException(DomainExceptionCode.INVALID_PAYMENT_AMOUNT);
    }

    return request.getCancelAmount();
  }

  private void savePaymentEvent(
      Payment payment,
      PaymentEventType eventType,
      PaymentStatus paymentStatus,
      PaymentEventPayload payload,
      LocalDateTime occurredAt
  ) {
    PaymentEvent event = PaymentEvent.of(
        CreateCommand.builder()
            .paymentId(payment.getId())
            .eventType(eventType)
            .paymentStatus(paymentStatus)
            .pgProvider(payment.getPgProvider())
            .requestPayload(payload.requestPayload())
            .responsePayload(payload.responsePayload())
            .failureCode(payload.failureCode())
            .failureMessage(payload.failureMessage())
            .occurredAt(occurredAt)
            .build()
    );
    paymentEventRepository.save(event);
  }

  private record PaymentEventPayload(
      String requestPayload,
      String responsePayload,
      String failureCode,
      String failureMessage
  ) {

    static PaymentEventPayload empty() {
      return new PaymentEventPayload(null, null, null, null);
    }

    static PaymentEventPayload success(String requestPayload, String responsePayload) {
      return new PaymentEventPayload(requestPayload, responsePayload, null, null);
    }

    static PaymentEventPayload failure(
        String requestPayload,
        String responsePayload,
        String failureCode,
        String failureMessage
    ) {
      return new PaymentEventPayload(requestPayload, responsePayload, failureCode, failureMessage);
    }
  }

  private record ConfirmContext(Long userId, Long paymentId) {
  }

  private record CancelContext(
      Long userId,
      Long paymentId,
      String paymentKey,
      BigDecimal cancelAmount
  ) {
  }

  private record PaymentTransactionResult(
      PaymentResponse response,
      DomainExceptionCode exceptionCode
  ) {

    static PaymentTransactionResult success(PaymentResponse response) {
      return new PaymentTransactionResult(response, null);
    }

    static PaymentTransactionResult failure(DomainExceptionCode exceptionCode) {
      return new PaymentTransactionResult(null, exceptionCode);
    }
  }

}
