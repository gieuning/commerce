import { useCallback, useEffect, useState } from "react";
import { productService } from "@/services/productService";
import type { ProductSummary } from "@/types/product";
import { getApiErrorMessage } from "@/utils/getApiErrorMessage";

interface InfiniteProductsState {
  errorMessage: string | null;
  hasNextPage: boolean;
  isInitialLoading: boolean;
  isLoadingMore: boolean;
  pageNumber: number;
  products: ProductSummary[];
  searchRequestId: number;
  submittedKeyword: string;
}

const mergeUniqueProducts = (
  currentProducts: ProductSummary[],
  nextProducts: ProductSummary[],
): ProductSummary[] => {
  const productMap = new Map<number, ProductSummary>();

  for (const product of currentProducts) {
    productMap.set(product.id, product);
  }

  for (const product of nextProducts) {
    productMap.set(product.id, product);
  }

  return [...productMap.values()];
};

export const useInfiniteProducts = () => {
  const [productState, setProductState] = useState<InfiniteProductsState>({
    errorMessage: null,
    hasNextPage: true,
    isInitialLoading: true,
    isLoadingMore: false,
    pageNumber: 0,
    products: [],
    searchRequestId: 0,
    submittedKeyword: "",
  });

  useEffect(() => {
    let isMounted = true;
    const isFirstPage = productState.pageNumber === 0;

    setProductState((currentState) => ({
      ...currentState,
      errorMessage: null,
      isInitialLoading: isFirstPage,
      isLoadingMore: !isFirstPage,
    }));

    productService
      .getProducts({
        keyword: productState.submittedKeyword || undefined,
        page: productState.pageNumber,
      })
      .then((productPage) => {
        if (!isMounted) {
          return;
        }

        setProductState((currentState) => ({
          ...currentState,
          hasNextPage: !productPage.last,
          products: isFirstPage
            ? productPage.content
            : mergeUniqueProducts(currentState.products, productPage.content),
        }));
      })
      .catch((error: unknown) => {
        if (isMounted) {
          setProductState((currentState) => ({
            ...currentState,
            errorMessage: getApiErrorMessage(error),
          }));
        }
      })
      .finally(() => {
        if (isMounted) {
          setProductState((currentState) => ({
            ...currentState,
            isInitialLoading: false,
            isLoadingMore: false,
          }));
        }
      });

    return () => {
      isMounted = false;
    };
  }, [productState.pageNumber, productState.searchRequestId, productState.submittedKeyword]);

  const searchProducts = useCallback((keyword: string) => {
    setProductState((currentState) => ({
      ...currentState,
      errorMessage: null,
      hasNextPage: true,
      pageNumber: 0,
      products: [],
      searchRequestId: currentState.searchRequestId + 1,
      submittedKeyword: keyword.trim(),
    }));
  }, []);

  const loadNextPage = useCallback(() => {
    setProductState((currentState) => {
      if (
        currentState.isInitialLoading ||
        currentState.isLoadingMore ||
        !currentState.hasNextPage ||
        currentState.errorMessage
      ) {
        return currentState;
      }

      return { ...currentState, pageNumber: currentState.pageNumber + 1 };
    });
  }, []);

  const retryProductsRequest = useCallback(() => {
    setProductState((currentState) => ({
      ...currentState,
      errorMessage: null,
      searchRequestId: currentState.searchRequestId + 1,
    }));
  }, []);

  return {
    ...productState,
    loadNextPage,
    retryProductsRequest,
    searchProducts,
  };
};
