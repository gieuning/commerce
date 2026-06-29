import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/Button";
import { EmptyState } from "@/components/EmptyState";
import { ErrorState } from "@/components/ErrorState";
import { LoadingState } from "@/components/LoadingState";
import { PageHeader } from "@/components/PageHeader";
import { MESSAGES } from "@/constants/messages";
import { ROUTES } from "@/constants/routes";
import { useAsyncAction } from "@/hooks/useAsyncAction";
import { cartService } from "@/services/cartService";
import { orderService } from "@/services/orderService";
import type { Cart } from "@/types/cart";
import { CartItemRow } from "@/features/cart/components/CartItemRow";
import { CartSummary } from "@/features/cart/components/CartSummary";

export const CartPage = () => {
  const navigate = useNavigate();
  const [cart, setCart] = useState<Cart | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const { errorMessage: actionErrorMessage, isLoading: isActionLoading, runAsyncAction } = useAsyncAction();

  const loadCart = () => {
    setIsLoading(true);
    setErrorMessage(null);
    cartService
      .getCart()
      .then(setCart)
      .catch(() => setErrorMessage(MESSAGES.COMMON.UNKNOWN_ERROR))
      .finally(() => setIsLoading(false));
  };

  useEffect(() => {
    loadCart();
  }, []);

  const handleQuantityChange = async (cartItemId: number, quantity: number) => {
    if (isActionLoading) {
      return;
    }

    const nextCart = await runAsyncAction(() =>
      cartService.updateCartItemQuantity(cartItemId, { quantity }),
    );

    if (nextCart) {
      setCart(nextCart);
    }
  };

  const handleRemove = async (cartItemId: number) => {
    if (isActionLoading) {
      return;
    }

    const removeResult = await runAsyncAction(() => cartService.removeCartItem(cartItemId));

    if (removeResult !== null) {
      loadCart();
    }
  };

  const handleClear = async () => {
    if (isActionLoading) {
      return;
    }

    const clearResult = await runAsyncAction(() => cartService.clearCart());

    if (clearResult !== null) {
      loadCart();
    }
  };

  const handleCreateOrder = async () => {
    if (!cart || isActionLoading) {
      return;
    }

    const orderResult = await runAsyncAction(() =>
      orderService.createOrder({
        items: cart.items.map((cartItem) => ({
          productId: cartItem.productId,
          optionCombinationId: cartItem.optionCombinationId ?? undefined,
          quantity: cartItem.quantity,
        })),
      }),
    );

    if (orderResult) {
      void navigate(ROUTES.ORDER_DETAIL(orderResult.orderId));
    }
  };

  if (isLoading) {
    return <LoadingState />;
  }

  if (errorMessage || !cart) {
    return <ErrorState message={errorMessage ?? MESSAGES.COMMON.UNKNOWN_ERROR} onRetry={loadCart} />;
  }

  return (
    <section className="grid gap-6">
      <PageHeader
        action={
          cart.items.length > 0 ? (
            <Button disabled={isActionLoading} onClick={handleClear} variant="secondary">
              전체 비우기
            </Button>
          ) : null
        }
        description="담아둔 상품을 확인하고 주문을 생성합니다."
        title="장바구니"
      />
      {actionErrorMessage ? <ErrorState message={actionErrorMessage} /> : null}
      {cart.items.length === 0 ? <EmptyState message={MESSAGES.CART.EMPTY} /> : null}
      {cart.items.length > 0 ? (
        <div className="grid gap-6 lg:grid-cols-[1fr_320px]">
          <div className="grid gap-4">
            {cart.items.map((cartItem) => (
              <CartItemRow
                disabled={isActionLoading}
                item={cartItem}
                key={cartItem.itemId}
                onQuantityChange={handleQuantityChange}
                onRemove={handleRemove}
              />
            ))}
          </div>
          <CartSummary cart={cart} isSubmitting={isActionLoading} onCreateOrder={handleCreateOrder} />
        </div>
      ) : null}
    </section>
  );
};
