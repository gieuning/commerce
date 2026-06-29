package com.gieun.commerce.domain.payment.dto.response;

import com.gieun.commerce.domain.payment.entity.PaymentReceipt;
import com.gieun.commerce.domain.payment.entity.PgProvider;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentReceiptResponse {

  Long paymentReceiptId;
  Long paymentId;
  PgProvider pgProvider;
  String paymentKey;
  String receiptUrl;
  BigDecimal totalAmount;
  BigDecimal suppliedAmount;
  BigDecimal vat;
  LocalDateTime issuedAt;

  public static PaymentReceiptResponse of(PaymentReceipt receipt) {
    return PaymentReceiptResponse.builder()
        .paymentReceiptId(receipt.getId())
        .paymentId(receipt.getPaymentId())
        .pgProvider(receipt.getPgProvider())
        .paymentKey(receipt.getPaymentKey())
        .receiptUrl(receipt.getReceiptUrl())
        .totalAmount(receipt.getTotalAmount())
        .suppliedAmount(receipt.getSuppliedAmount())
        .vat(receipt.getVat())
        .issuedAt(receipt.getIssuedAt())
        .build();
  }
}
