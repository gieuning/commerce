import { ShoppingCart } from "lucide-react";
import { type FormEvent, useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { Button } from "@/components/Button";
import { ErrorState } from "@/components/ErrorState";
import { LoadingState } from "@/components/LoadingState";
import { PageHeader } from "@/components/PageHeader";
import { PriceText } from "@/components/PriceText";
import { QuantityStepper } from "@/components/QuantityStepper";
import { StatusBadge } from "@/components/StatusBadge";
import { MESSAGES } from "@/constants/messages";
import { ROUTES } from "@/constants/routes";
import { PRODUCT_STATUS_LABELS, PRODUCT_STATUS_TONES } from "@/constants/statusLabels";
import { useAsyncAction } from "@/hooks/useAsyncAction";
import { cartService } from "@/services/cartService";
import { productService } from "@/services/productService";
import { PRODUCT_STATUS, type ProductDetail } from "@/types/product";
import { getApiErrorMessage } from "@/utils/getApiErrorMessage";
import { ProductOptionPicker } from "@/features/products/components/ProductOptionPicker";

const parseProductId = (productId: string | undefined): number | null => {
  if (!productId) {
    return null;
  }

  const parsedProductId = Number(productId);
  return Number.isFinite(parsedProductId) ? parsedProductId : null;
};

export const ProductDetailPage = () => {
  const navigate = useNavigate();
  const params = useParams();
  const productId = parseProductId(params.productId);
  const [product, setProduct] = useState<ProductDetail | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [selectedCombinationId, setSelectedCombinationId] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const {
    errorMessage: actionErrorMessage,
    isLoading: isActionLoading,
    runAsyncAction,
  } = useAsyncAction();

  useEffect(() => {
    if (productId === null) {
      setErrorMessage(MESSAGES.COMMON.UNKNOWN_ERROR);
      setIsLoading(false);
      return;
    }

    let isMounted = true;
    setIsLoading(true);
    setErrorMessage(null);

    productService
      .getProduct(productId)
      .then((nextProduct) => {
        if (isMounted) {
          setProduct(nextProduct);
        }
      })
      .catch((error: unknown) => {
        if (isMounted) {
          setErrorMessage(getApiErrorMessage(error));
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
  }, [productId]);

  const selectedCombination = useMemo(
    () =>
      product?.combinations.find((combination) => combination.id === selectedCombinationId) ?? null,
    [product, selectedCombinationId],
  );

  const displayPrice = selectedCombination?.finalPrice ?? product?.price ?? "0";
  const availableStock = selectedCombination?.stock ?? product?.stock ?? 0;
  const hasValidCombination =
    !product?.hasOptions ||
    (selectedCombination !== null &&
      selectedCombination.status === PRODUCT_STATUS.FOR_SALE &&
      selectedCombination.stock > 0);

  const canAddToCart =
    product !== null &&
    product.status === PRODUCT_STATUS.FOR_SALE &&
    availableStock > 0 &&
    hasValidCombination;

  const handleAddCart = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (productId === null || !canAddToCart) {
      return;
    }

    const cartResult = await runAsyncAction(() =>
      cartService.addCartItem({
        productId,
        optionCombinationId: selectedCombination?.id ?? undefined,
        quantity,
      }),
    );

    if (cartResult !== null) {
      void navigate(ROUTES.CART);
    }
  };

  if (isLoading) {
    return <LoadingState />;
  }

  if (errorMessage || !product) {
    return <ErrorState message={errorMessage ?? MESSAGES.COMMON.UNKNOWN_ERROR} />;
  }

  return (
    <section className="grid gap-8">
      <PageHeader title={product.name} description={product.description ?? undefined} />
      <div className="grid gap-8 lg:grid-cols-[minmax(0,1fr)_380px]">
        <div className="overflow-hidden rounded-card border border-line bg-surface">
          {product.imageUrl ? (
            <img alt={product.name} className="aspect-[4/3] w-full object-cover" src={product.imageUrl} />
          ) : (
            <div className="grid aspect-[4/3] place-items-center bg-line text-ink-soft">
              {MESSAGES.PRODUCT.IMAGE_PLACEHOLDER}
            </div>
          )}
        </div>
        <form className="grid h-fit gap-5 rounded-card border border-line bg-surface p-5" onSubmit={handleAddCart}>
          <div className="flex items-center justify-between gap-3">
            <PriceText amount={displayPrice} className="text-2xl font-bold" />
            <StatusBadge
              label={PRODUCT_STATUS_LABELS[product.status]}
              tone={PRODUCT_STATUS_TONES[product.status]}
            />
          </div>
          {product.hasOptions ? (
            <ProductOptionPicker
              combinations={product.combinations}
              onChange={setSelectedCombinationId}
              selectedCombinationId={selectedCombinationId}
            />
          ) : null}
          <QuantityStepper
            label="수량"
            max={availableStock}
            onChange={setQuantity}
            value={quantity}
          />
          {actionErrorMessage ? <ErrorState message={actionErrorMessage} /> : null}
          <Button disabled={!canAddToCart || isActionLoading} icon={<ShoppingCart size={16} />} type="submit">
            {isActionLoading ? "담는 중" : "장바구니 담기"}
          </Button>
        </form>
      </div>
    </section>
  );
};
