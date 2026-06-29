import { Search } from "lucide-react";
import { type FormEvent, useEffect, useState } from "react";
import { Button } from "@/components/Button";
import { EmptyState } from "@/components/EmptyState";
import { ErrorState } from "@/components/ErrorState";
import { Input } from "@/components/Input";
import { LoadingState } from "@/components/LoadingState";
import { PageHeader } from "@/components/PageHeader";
import { MESSAGES } from "@/constants/messages";
import { productService } from "@/services/productService";
import type { PageResult } from "@/types/api";
import type { ProductSummary } from "@/types/product";
import { ProductCard } from "@/features/products/components/ProductCard";

export const ProductListPage = () => {
  const [keyword, setKeyword] = useState("");
  const [submittedKeyword, setSubmittedKeyword] = useState("");
  const [productPage, setProductPage] = useState<PageResult<ProductSummary> | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;
    setIsLoading(true);
    setErrorMessage(null);

    productService
      .getProducts({ keyword: submittedKeyword || undefined })
      .then((nextProductPage) => {
        if (isMounted) {
          setProductPage(nextProductPage);
        }
      })
      .catch(() => {
        if (isMounted) {
          setErrorMessage(MESSAGES.COMMON.UNKNOWN_ERROR);
        }
      })
      .finally(() => {
        if (isMounted) {
          setIsLoading(false);
        }
      });

    return () => {
      isMounted = false;
    };
  }, [submittedKeyword]);

  const handleSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmittedKeyword(keyword.trim());
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
      {isLoading ? <LoadingState /> : null}
      {errorMessage ? <ErrorState message={errorMessage} /> : null}
      {!isLoading && !errorMessage && productPage?.content.length === 0 ? (
        <EmptyState message={MESSAGES.PRODUCT.EMPTY} />
      ) : null}
      {!isLoading && !errorMessage && productPage && productPage.content.length > 0 ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {productPage.content.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
      ) : null}
    </section>
  );
};
