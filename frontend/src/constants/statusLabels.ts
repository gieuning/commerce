import type { StatusBadgeTone } from "@/components/StatusBadge";
import { ORDER_STATUS, type OrderStatus } from "@/types/order";
import { PAYMENT_STATUS, type PaymentStatus } from "@/types/payment";
import { PRODUCT_STATUS, type ProductStatus } from "@/types/product";

export const ORDER_STATUS_LABELS: Record<OrderStatus, string> = {
  [ORDER_STATUS.CREATED]: "결제 대기",
  [ORDER_STATUS.PAID]: "결제 완료",
  [ORDER_STATUS.CANCELLED]: "주문 취소",
};

export const PRODUCT_STATUS_LABELS: Record<ProductStatus, string> = {
  [PRODUCT_STATUS.FOR_SALE]: "판매중",
  [PRODUCT_STATUS.STOP_SALE]: "판매중지",
  [PRODUCT_STATUS.OUT_OF_STOCK]: "품절",
};

export const PAYMENT_STATUS_LABELS: Record<PaymentStatus, string> = {
  [PAYMENT_STATUS.REQUESTED]: "결제 요청",
  [PAYMENT_STATUS.APPROVED]: "결제 승인",
  [PAYMENT_STATUS.FAILED]: "결제 실패",
  [PAYMENT_STATUS.CANCELLED]: "결제 취소",
};

export const ORDER_STATUS_TONES: Record<OrderStatus, StatusBadgeTone> = {
  [ORDER_STATUS.CREATED]: "warning",
  [ORDER_STATUS.PAID]: "success",
  [ORDER_STATUS.CANCELLED]: "error",
};

export const PRODUCT_STATUS_TONES: Record<ProductStatus, StatusBadgeTone> = {
  [PRODUCT_STATUS.FOR_SALE]: "success",
  [PRODUCT_STATUS.STOP_SALE]: "warning",
  [PRODUCT_STATUS.OUT_OF_STOCK]: "warning",
};

export const PAYMENT_STATUS_TONES: Record<PaymentStatus, StatusBadgeTone> = {
  [PAYMENT_STATUS.REQUESTED]: "warning",
  [PAYMENT_STATUS.APPROVED]: "success",
  [PAYMENT_STATUS.FAILED]: "error",
  [PAYMENT_STATUS.CANCELLED]: "error",
};
