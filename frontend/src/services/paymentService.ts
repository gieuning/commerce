import { API_ENDPOINTS } from "@/constants/api";
import { apiClient } from "@/services/apiClient";
import type {
  Payment,
  PaymentCancelRequest,
  PaymentConfirmRequest,
  PaymentCreateRequest,
} from "@/types/payment";

export const paymentService = {
  requestPayment: (requestBody: PaymentCreateRequest): Promise<Payment> =>
    apiClient.post<Payment, PaymentCreateRequest>(API_ENDPOINTS.PAYMENTS.LIST, requestBody),
  confirmPayment: (requestBody: PaymentConfirmRequest): Promise<Payment> =>
    apiClient.post<Payment, PaymentConfirmRequest>(API_ENDPOINTS.PAYMENTS.CONFIRM, requestBody),
  getPayment: (paymentId: number): Promise<Payment> =>
    apiClient.get<Payment>(API_ENDPOINTS.PAYMENTS.DETAIL(paymentId)),
  cancelPayment: (paymentId: number, requestBody: PaymentCancelRequest): Promise<Payment> =>
    apiClient.post<Payment, PaymentCancelRequest>(API_ENDPOINTS.PAYMENTS.CANCEL(paymentId), requestBody),
};
