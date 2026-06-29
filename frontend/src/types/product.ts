export const PRODUCT_STATUS = {
  FOR_SALE: "FOR_SALE",
  STOP_SALE: "STOP_SALE",
  OUT_OF_STOCK: "OUT_OF_STOCK",
} as const;

export type ProductStatus = (typeof PRODUCT_STATUS)[keyof typeof PRODUCT_STATUS];

export interface ProductSummary {
  id: number;
  name: string;
  description: string | null;
  price: string;
  stock: number;
  status: ProductStatus;
  imageUrl: string | null;
  createdAt: string;
}

export interface OptionGroup {
  name: string;
  values: string[];
}

export interface OptionCombination {
  id: number;
  optionValues: string[];
  additionalPrice: string;
  finalPrice: string;
  stock: number;
  status: ProductStatus;
}
export interface ProductDetail extends ProductSummary {
  hasOptions: boolean;
  optionGroups: OptionGroup[];
  combinations: OptionCombination[];
}

export interface ProductCreateRequest {
  name: string;
  description?: string;
  price: string;
  stock: number;
  imageUrl?: string;
}

export interface ProductUpdateRequest {
  name: string;
  description?: string;
  price: string;
  imageUrl?: string;
}

export interface StockUpdateRequest {
  stock: number;
}
