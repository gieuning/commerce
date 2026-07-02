import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { EmptyState } from "@/components/EmptyState";
import { ErrorState } from "@/components/ErrorState";
import { LoadingState } from "@/components/LoadingState";
import { PageHeader } from "@/components/PageHeader";
import { MESSAGES } from "@/constants/messages";
import { ProductCard } from "@/features/products/components/ProductCard";
import { useInfiniteProducts } from "@/features/products/hooks/useInfiniteProducts";

export const ProductListPage = () => {
  const [searchParams] = useSearchParams();
  const keyword = (searchParams.get("keyword") ?? "").trim();
  const [loadMoreElement, setLoadMoreElement] = useState<HTMLDivElement | null>(null);
  const {
    errorMessage,
    hasNextPage,
    isInitialLoading,
    isLoadingMore,
    loadNextPage,
    products,
    retryProductsRequest,
  } = useInfiniteProducts(keyword);
  const hasProducts = products.length > 0;
  const shouldShowInitialError = !isInitialLoading && !hasProducts && errorMessage;

  useEffect(() => {
    if (!loadMoreElement) {
      return undefined;
    }

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry?.isIntersecting) {
          loadNextPage();
        }
      },
      { rootMargin: "240px 0px" },
    );

    observer.observe(loadMoreElement);

    return () => observer.disconnect();
  }, [loadMoreElement, loadNextPage]);

  return (
    <section className="grid gap-6">
      <PageHeader
        description={keyword ? MESSAGES.PRODUCT.SEARCH_RESULT(keyword) : MESSAGES.PRODUCT.LIST_DESCRIPTION}
        title="상품"
      />
      {isInitialLoading ? <LoadingState /> : null}
      {shouldShowInitialError ? <ErrorState message={errorMessage} onRetry={retryProductsRequest} /> : null}
      {!isInitialLoading && !errorMessage && !hasProducts ? (
        <EmptyState message={MESSAGES.PRODUCT.EMPTY} />
      ) : null}
      {!isInitialLoading && hasProducts ? (
        <>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {products.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
          <div ref={setLoadMoreElement} className="flex min-h-16 items-center justify-center text-sm text-ink-soft">
            {errorMessage ? <ErrorState message={errorMessage} onRetry={retryProductsRequest} /> : null}
            {!errorMessage && isLoadingMore ? MESSAGES.PRODUCT.LOADING_MORE : null}
            {!errorMessage && !isLoadingMore && !hasNextPage ? MESSAGES.PRODUCT.END_OF_LIST : null}
          </div>
        </>
      ) : null}
    </section>
  );
};
