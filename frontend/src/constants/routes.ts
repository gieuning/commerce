export const ROUTES = {
  HOME: "/",
  LOGIN: "/login",
  SIGNUP: "/signup",
  PRODUCTS: "/products",
  PRODUCT_DETAIL: (productId: number) => `/products/${productId}`,
  CART: "/cart",
  ORDERS: "/orders",
  ORDER_DETAIL: (orderId: number) => `/orders/${orderId}`,
  ADMIN_PRODUCTS: "/admin/products",
  ADMIN_PRODUCT_NEW: "/admin/products/new",
  ADMIN_PRODUCT_EDIT: (productId: number) => `/admin/products/${productId}/edit`,
} as const;
