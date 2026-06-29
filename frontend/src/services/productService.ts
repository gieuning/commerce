import { API_ENDPOINTS } from "@/constants/api";
import { DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE } from "@/constants/pagination";
import type { PageResult } from "@/types/api";
import type {
  ProductCreateRequest,
  ProductDetail,
  ProductSummary,
  ProductUpdateRequest,
  StockUpdateRequest,
} from "@/types/product";
import { apiClient } from "@/services/apiClient";

interface ProductListParams {
  keyword?: string;
  page?: number;
  size?: number;
}

const normalizePageNumber = (page: number | undefined): number =>
  Number.isInteger(page) && page >= 0 ? page : DEFAULT_PAGE_NUMBER;

const normalizePageSize = (size: number | undefined): number =>
  Number.isInteger(size) && size > 0 ? size : DEFAULT_PAGE_SIZE;

const createProductListEndpoint = (params: ProductListParams): string => {
  const searchParams = new URLSearchParams();
  searchParams.set("page", String(normalizePageNumber(params.page)));
  searchParams.set("size", String(normalizePageSize(params.size)));

  if (params.keyword) {
    searchParams.set("keyword", params.keyword);
  }

  return `${API_ENDPOINTS.PRODUCTS.LIST}?${searchParams.toString()}`;
};

export const productService = {
  getProducts: (params: ProductListParams = {}): Promise<PageResult<ProductSummary>> =>
    apiClient.get<PageResult<ProductSummary>>(createProductListEndpoint(params)),
  getProduct: (productId: number): Promise<ProductDetail> =>
    apiClient.get<ProductDetail>(API_ENDPOINTS.PRODUCTS.DETAIL(productId)),
  createProduct: (requestBody: ProductCreateRequest): Promise<ProductDetail> =>
    apiClient.post<ProductDetail, ProductCreateRequest>(API_ENDPOINTS.PRODUCTS.CREATE, requestBody),
  updateProduct: (productId: number, requestBody: ProductUpdateRequest): Promise<ProductDetail> =>
    apiClient.put<ProductDetail, ProductUpdateRequest>(
      API_ENDPOINTS.PRODUCTS.UPDATE(productId),
      requestBody,
    ),
  updateStock: (productId: number, requestBody: StockUpdateRequest): Promise<ProductDetail> =>
    apiClient.patch<ProductDetail, StockUpdateRequest>(
      API_ENDPOINTS.PRODUCTS.STOCK(productId),
      requestBody,
    ),
  deleteProduct: (productId: number): Promise<void> =>
    apiClient.delete<void>(API_ENDPOINTS.PRODUCTS.DELETE(productId)),
};
