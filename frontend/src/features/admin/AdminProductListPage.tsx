import { Plus } from "lucide-react";
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Button } from "@/components/Button";
import { EmptyState } from "@/components/EmptyState";
import { ErrorState } from "@/components/ErrorState";
import { LoadingState } from "@/components/LoadingState";
import { PageHeader } from "@/components/PageHeader";
import { PriceText } from "@/components/PriceText";
import { StatusBadge } from "@/components/StatusBadge";
import { MESSAGES } from "@/constants/messages";
import { ROUTES } from "@/constants/routes";
import { PRODUCT_STATUS_LABELS, PRODUCT_STATUS_TONES } from "@/constants/statusLabels";
import { useAsyncAction } from "@/hooks/useAsyncAction";
import { productService } from "@/services/productService";
import type { PageResult } from "@/types/api";
import type { ProductSummary } from "@/types/product";
import { getApiErrorMessage } from "@/utils/getApiErrorMessage";

export const AdminProductListPage = () => {
  const [productPage, setProductPage] = useState<PageResult<ProductSummary> | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const { errorMessage: actionErrorMessage, isLoading: isActionLoading, runAsyncAction } = useAsyncAction();

  const loadProducts = () => {
    setIsLoading(true);
    setErrorMessage(null);
    productService
      .getProducts()
      .then(setProductPage)
      .catch((error: unknown) => setErrorMessage(getApiErrorMessage(error)))
      .finally(() => setIsLoading(false));
  };

  useEffect(() => {
    loadProducts();
  }, []);

  const handleDelete = async (productId: number) => {
    const deleteResult = await runAsyncAction(() => productService.deleteProduct(productId));

    if (deleteResult !== null) {
      loadProducts();
    }
  };

  return (
    <section className="grid gap-6">
      <PageHeader
        action={
          <Link to={ROUTES.ADMIN_PRODUCT_NEW}>
            <Button icon={<Plus size={16} />}>상품 등록</Button>
          </Link>
        }
        description="상품 기본 정보와 재고를 관리합니다."
        title="관리자 상품"
      />
      {isLoading ? <LoadingState /> : null}
      {errorMessage ? <ErrorState message={errorMessage} onRetry={loadProducts} /> : null}
      {actionErrorMessage ? <ErrorState message={actionErrorMessage} /> : null}
      {!isLoading && !errorMessage && productPage?.content.length === 0 ? (
        <EmptyState message={MESSAGES.PRODUCT.EMPTY} />
      ) : null}
      {!isLoading && !errorMessage && productPage && productPage.content.length > 0 ? (
        <div className="overflow-hidden rounded-card border border-line bg-surface">
          <table className="w-full text-left text-sm">
            <thead className="bg-line/50 text-ink-soft">
              <tr>
                <th className="px-4 py-3">상품명</th>
                <th className="px-4 py-3">가격</th>
                <th className="px-4 py-3">재고</th>
                <th className="px-4 py-3">상태</th>
                <th className="px-4 py-3 text-right">관리</th>
              </tr>
            </thead>
            <tbody>
              {productPage.content.map((product) => (
                <tr className="border-t border-line" key={product.id}>
                  <td className="px-4 py-3 font-semibold">{product.name}</td>
                  <td className="px-4 py-3">
                    <PriceText amount={product.price} />
                  </td>
                  <td className="px-4 py-3">{product.stock}</td>
                  <td className="px-4 py-3">
                    <StatusBadge
                      label={PRODUCT_STATUS_LABELS[product.status]}
                      tone={PRODUCT_STATUS_TONES[product.status]}
                    />
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex justify-end gap-2">
                      <Link to={ROUTES.ADMIN_PRODUCT_EDIT(product.id)}>
                        <Button size="sm" variant="secondary">
                          수정
                        </Button>
                      </Link>
                      <Button
                        disabled={isActionLoading}
                        onClick={() => handleDelete(product.id)}
                        size="sm"
                        variant="danger"
                      >
                        삭제
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}
    </section>
  );
};
