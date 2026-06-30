import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { ErrorState } from "@/components/ErrorState";
import { LoadingState } from "@/components/LoadingState";
import { PageHeader } from "@/components/PageHeader";
import { MESSAGES } from "@/constants/messages";
import { ROUTES } from "@/constants/routes";
import { useAsyncAction } from "@/hooks/useAsyncAction";
import { productService } from "@/services/productService";
import type { ProductCreateRequest, ProductDetail, ProductUpdateRequest } from "@/types/product";
import { getApiErrorMessage } from "@/utils/getApiErrorMessage";
import { parseRouteNumber } from "@/utils/parseRouteNumber";
import { ProductForm } from "@/features/admin/components/ProductForm";
import { StockControl } from "@/features/admin/components/StockControl";

export const AdminProductFormPage = () => {
  const navigate = useNavigate();
  const params = useParams();
  const productId = parseRouteNumber(params.productId);
  const isEditMode = productId !== null;
  const [product, setProduct] = useState<ProductDetail | null>(null);
  const [isLoading, setIsLoading] = useState(isEditMode);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const { errorMessage: actionErrorMessage, isLoading: isActionLoading, runAsyncAction } = useAsyncAction();

  useEffect(() => {
    if (!isEditMode || productId === null) {
      setProduct(null);
      setErrorMessage(null);
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    setErrorMessage(null);
    setProduct(null);

    void productService
      .getProduct(productId)
      .then(setProduct)
      .catch((error: unknown) =>
        setErrorMessage(`${MESSAGES.ADMIN_PRODUCT.LOAD_FAILED} ${getApiErrorMessage(error)}`),
      )
      .finally(() => setIsLoading(false));
  }, [isEditMode, productId]);

  const handleSubmit = async (requestBody: ProductCreateRequest | ProductUpdateRequest) => {
    const productResult =
      isEditMode && productId !== null
        ? await runAsyncAction(() => productService.updateProduct(productId, requestBody))
        : await runAsyncAction(() => productService.createProduct(requestBody as ProductCreateRequest));

    if (productResult) {
      void navigate(ROUTES.ADMIN_PRODUCTS);
    }
  };

  const handleStockSubmit = async (stock: number) => {
    if (productId === null) {
      return;
    }

    const productResult = await runAsyncAction(() =>
      productService.updateStock(productId, { stock }),
    );

    if (productResult) {
      setProduct(productResult);
    }
  };

  if (isLoading) {
    return <LoadingState />;
  }

  if (errorMessage) {
    return <ErrorState message={errorMessage} />;
  }

  return (
    <section className="grid gap-6">
      <PageHeader
        description={MESSAGES.ADMIN_PRODUCT.FORM_DESCRIPTION}
        title={isEditMode ? "상품 수정" : "상품 등록"}
      />
      {actionErrorMessage ? <ErrorState message={actionErrorMessage} /> : null}
      <div className="grid gap-6 lg:grid-cols-[1fr_320px]">
        <ProductForm isSubmitting={isActionLoading} onSubmit={handleSubmit} product={product ?? undefined} />
        {isEditMode && product ? (
          <StockControl
            initialStock={product.stock}
            isSubmitting={isActionLoading}
            onSubmit={handleStockSubmit}
          />
        ) : null}
      </div>
    </section>
  );
};
