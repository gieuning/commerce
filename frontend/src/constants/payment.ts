const configuredTossClientKey: unknown = import.meta.env.VITE_TOSS_CLIENT_KEY;
const configuredPaymentRedirectBaseUrl: unknown = import.meta.env.VITE_PAYMENT_REDIRECT_BASE_URL;

export const TOSS_PAYMENTS_CLIENT_KEY =
  typeof configuredTossClientKey === "string" ? configuredTossClientKey : "";

export const PAYMENT_REDIRECT_BASE_URL =
  typeof configuredPaymentRedirectBaseUrl === "string" && configuredPaymentRedirectBaseUrl.length > 0
    ? configuredPaymentRedirectBaseUrl.replace(/\/$/, "")
    : window.location.origin;

export const TOSS_PAYMENTS_SCRIPT_URL = "https://js.tosspayments.com/v1/payment";

export const PAYMENT_METHOD_LABELS = {
  CARD: "카드",
  VIRTUAL_ACCOUNT: "가상계좌",
  TRANSFER: "계좌이체",
  MOBILE_PHONE: "휴대폰",
  EASY_PAY: "간편결제",
} as const;

export const PAYMENT_METHOD_OPTIONS = [
  { label: PAYMENT_METHOD_LABELS.CARD, value: "CARD" },
  { label: PAYMENT_METHOD_LABELS.EASY_PAY, value: "EASY_PAY" },
  { label: PAYMENT_METHOD_LABELS.TRANSFER, value: "TRANSFER" },
  { label: PAYMENT_METHOD_LABELS.VIRTUAL_ACCOUNT, value: "VIRTUAL_ACCOUNT" },
  { label: PAYMENT_METHOD_LABELS.MOBILE_PHONE, value: "MOBILE_PHONE" },
] as const;
