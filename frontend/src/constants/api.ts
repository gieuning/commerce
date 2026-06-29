const configuredApiBaseUrl: unknown = import.meta.env.VITE_API_BASE_URL;

export const API_BASE_URL =
  typeof configuredApiBaseUrl === "string" && configuredApiBaseUrl.length > 0
    ? configuredApiBaseUrl
    : "/api";

export const API_ENDPOINTS = {
  USERS: {
    SIGNUP: "/users/signup",
    LOGIN: "/users/login",
    ME: "/users/me",
  },
  PRODUCTS: {
    LIST: "/products",
    DETAIL: (productId: number) => `/products/${productId}`,
    CREATE: "/products",
    UPDATE: (productId: number) => `/products/${productId}`,
    STOCK: (productId: number) => `/products/${productId}/stock`,
    DELETE: (productId: number) => `/products/${productId}`,
  },
  CART: {
    DETAIL: "/cart",
    ITEMS: "/cart/items",
    ITEM: (cartItemId: number) => `/cart/items/${cartItemId}`,
    ITEM_OPTION: (cartItemId: number) => `/cart/items/${cartItemId}/option`,
  },
  ORDERS: {
    LIST: "/orders",
    DETAIL: (orderId: number) => `/orders/${orderId}`,
    CANCEL: (orderId: number) => `/orders/${orderId}/cancel`,
  },
} as const;
