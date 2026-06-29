import { API_ENDPOINTS } from "@/constants/api";
import type {
  Cart,
  CartItemAddRequest,
  CartItemOptionUpdateRequest,
  CartItemUpdateRequest,
} from "@/types/cart";
import { apiClient } from "@/services/apiClient";

export const cartService = {
  getCart: (): Promise<Cart> => apiClient.get<Cart>(API_ENDPOINTS.CART.DETAIL),
  addCartItem: (requestBody: CartItemAddRequest): Promise<Cart> =>
    apiClient.post<Cart, CartItemAddRequest>(API_ENDPOINTS.CART.ITEMS, requestBody),
  updateCartItemQuantity: (cartItemId: number, requestBody: CartItemUpdateRequest): Promise<Cart> =>
    apiClient.patch<Cart, CartItemUpdateRequest>(API_ENDPOINTS.CART.ITEM(cartItemId), requestBody),
  updateCartItemOption: (
    cartItemId: number,
    requestBody: CartItemOptionUpdateRequest,
  ): Promise<Cart> =>
    apiClient.patch<Cart, CartItemOptionUpdateRequest>(
      API_ENDPOINTS.CART.ITEM_OPTION(cartItemId),
      requestBody,
    ),
  removeCartItem: (cartItemId: number): Promise<void> =>
    apiClient.delete<void>(API_ENDPOINTS.CART.ITEM(cartItemId)),
  clearCart: (): Promise<void> => apiClient.delete<void>(API_ENDPOINTS.CART.ITEMS),
};
