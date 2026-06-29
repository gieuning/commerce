export interface CartItem {
  itemId: number;
  productId: number;
  productName: string | null;
  imageUrl: string | null;
  optionCombinationId: number | null;
  optionValues: string[];
  unitPrice: string;
  quantity: number;
  subtotal: string;
  stock: number;
  available: boolean;
  unavailableReason?: string;
}

export interface Cart {
  items: CartItem[];
  totalQuantity: number;
  totalPrice: string;
}

export interface CartItemAddRequest {
  productId: number;
  optionCombinationId?: number;
  quantity: number;
}

export interface CartItemUpdateRequest {
  quantity: number;
}

export interface CartItemOptionUpdateRequest {
  optionCombinationId: number;
}
