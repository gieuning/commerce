import { Search } from "lucide-react";
import { type FormEvent, useEffect, useState } from "react";
import { Button } from "@/components/Button";
import { EmptyState } from "@/components/EmptyState";
import { ErrorState } from "@/components/ErrorState";
import { Input } from "@/components/Input";
import { LoadingState } from "@/components/LoadingState";
import { PageHeader } from "@/components/PageHeader";
import { MESSAGES } from "@/constants/messages";
import { ProductCard } from "@/features/products/components/ProductCard";
import { useInfiniteProducts } from "@/features/products/hooks/useInfiniteProducts";

export const ProductListPage = () => {
  const [keyword, setKeyword] = useState("");
  const [loadMoreElement, setLoadMoreElement] = useState<HTMLDivElement | null>(null);
  const {
    errorMessage,
    hasNextPage,
    isInitialLoading,
    isLoadingMore,
    loadNextPage,
    products,
    retryProductsRequest,
    searchProducts,
  } = useInfiniteProducts();
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

  const handleSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    searchProducts(keyword);
  };

  return (
    <section className="grid gap-6">
      <PageHeader
        description="상품을 둘러보고 장바구니에 담아 주문을 생성할 수 있습니다."
        title="상품"
      />
      <form className="flex flex-col gap-3 sm:flex-row sm:items-end" onSubmit={handleSearch}>
        <div className="flex-1">
          <Input
            label="상품 검색"
            name="keyword"
            onChange={(event) => setKeyword(event.target.value)}
            placeholder="상품명을 입력하세요"
            value={keyword}
          />
        </div>
        <Button icon={<Search size={16} />} type="submit">
          검색
        </Button>
      </form>
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
