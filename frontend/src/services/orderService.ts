import { API_ENDPOINTS } from "@/constants/api";
import { DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE } from "@/constants/pagination";
import type { PageResult } from "@/types/api";
import type { Order, OrderCreateRequest } from "@/types/order";
import { apiClient } from "@/services/apiClient";

interface OrderListParams {
  page?: number;
  size?: number;
  status?: string;
}

const createOrderListEndpoint = (params: OrderListParams): string => {
  const searchParams = new URLSearchParams();
  searchParams.set("page", String(params.page ?? DEFAULT_PAGE_NUMBER));
  searchParams.set("size", String(params.size ?? DEFAULT_PAGE_SIZE));

  if (params.status) {
    searchParams.set("status", params.status);
  }

  return `${API_ENDPOINTS.ORDERS.LIST}?${searchParams.toString()}`;
};

export const orderService = {
  createOrder: (requestBody: OrderCreateRequest): Promise<Order> =>
    apiClient.post<Order, OrderCreateRequest>(API_ENDPOINTS.ORDERS.LIST, requestBody),
  getOrders: (params: OrderListParams = {}): Promise<PageResult<Order>> =>
    apiClient.get<PageResult<Order>>(createOrderListEndpoint(params)),
  getOrder: (orderId: number): Promise<Order> =>
    apiClient.get<Order>(API_ENDPOINTS.ORDERS.DETAIL(orderId)),
  cancelOrder: (orderId: number): Promise<Order> =>
    apiClient.patch<Order, Record<string, never>>(API_ENDPOINTS.ORDERS.CANCEL(orderId), {}),
};
