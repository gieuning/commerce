export const PAYMENT_METHOD = {
  CARD: "CARD",
  VIRTUAL_ACCOUNT: "VIRTUAL_ACCOUNT",
  TRANSFER: "TRANSFER",
  MOBILE_PHONE: "MOBILE_PHONE",
  EASY_PAY: "EASY_PAY",
} as const;

export const PAYMENT_STATUS = {
  REQUESTED: "REQUESTED",
  APPROVED: "APPROVED",
  FAILED: "FAILED",
  CANCELLED: "CANCELLED",
} as const;

export type PaymentMethod = (typeof PAYMENT_METHOD)[keyof typeof PAYMENT_METHOD];
export type PaymentStatus = (typeof PAYMENT_STATUS)[keyof typeof PAYMENT_STATUS];
export type PgProvider = "TOSS";

export interface PaymentCreateRequest {
  orderId: number;
  method: PaymentMethod;
}

export interface PaymentConfirmRequest {
  paymentKey: string;
  merchantOrderId: string;
  amount: string;
}

export interface PaymentCancelRequest {
  cancelReason: string;
  cancelAmount?: string;
}

export interface Payment {
  paymentId: number;
  orderId: number;
  userId: number;
  paymentKey: string | null;
  merchantOrderId: string;
  pgProvider: PgProvider;
  method: PaymentMethod;
  status: PaymentStatus;
  amount: string;
  approvedAt: string | null;
  cancelledAt: string | null;
  failureCode: string | null;
  failureMessage: string | null;
}
