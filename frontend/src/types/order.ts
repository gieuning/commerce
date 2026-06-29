export const ORDER_STATUS = {
  CREATED: "CREATED",
  PAID: "PAID",
  CANCELLED: "CANCELLED",
} as const;

export type OrderStatus = (typeof ORDER_STATUS)[keyof typeof ORDER_STATUS];

export interface OrderItemRequest {
  productId: number;
  optionCombinationId?: number;
  quantity: number;
}

export interface OrderCreateRequest {
  items: OrderItemRequest[];
}

export interface OrderItem {
  itemId: number;
  productId: number;
  optionCombinationId: number | null;
  productName: string;
  optionValues: string | null;
  unitPrice: string;
  quantity: number;
  subtotal: string;
}

export interface Order {
  orderId: number;
  status: OrderStatus;
  totalProductPrice: string;
  discountAmount: string;
  shippingFee: string;
  totalPrice: string;
  orderedAt: string;
  items: OrderItem[];
}
