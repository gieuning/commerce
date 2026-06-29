import { CreditCard } from "lucide-react";
import { useCallback, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { Button } from "@/components/Button";
import { ErrorState } from "@/components/ErrorState";
import { LoadingState } from "@/components/LoadingState";
import { PageHeader } from "@/components/PageHeader";
import { PriceText } from "@/components/PriceText";
import { StatusBadge } from "@/components/StatusBadge";
import { MESSAGES } from "@/constants/messages";
import { useAsyncAction } from "@/hooks/useAsyncAction";
import { orderService } from "@/services/orderService";
import { ORDER_STATUS, type Order } from "@/types/order";
import { formatDateTime } from "@/utils/formatDateTime";
import { OrderItemTable } from "@/features/orders/components/OrderItemTable";

const parseOrderId = (orderId: string | undefined): number | null => {
  if (!orderId) {
    return null;
  }

  const parsedOrderId = Number(orderId);
  return Number.isFinite(parsedOrderId) ? parsedOrderId : null;
};

export const OrderDetailPage = () => {
  const params = useParams();
  const orderId = parseOrderId(params.orderId);
  const [order, setOrder] = useState<Order | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const { errorMessage: actionErrorMessage, isLoading: isActionLoading, runAsyncAction } = useAsyncAction();

  const loadOrder = useCallback(() => {
    if (orderId === null) {
      setErrorMessage(MESSAGES.COMMON.UNKNOWN_ERROR);
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    setErrorMessage(null);
    orderService
      .getOrder(orderId)
      .then(setOrder)
      .catch(() => setErrorMessage(MESSAGES.COMMON.UNKNOWN_ERROR))
      .finally(() => setIsLoading(false));
  }, [orderId]);

  useEffect(() => {
    loadOrder();
  }, [loadOrder]);

  const handleCancel = async () => {
    if (!order) {
      return;
    }

    const nextOrder = await runAsyncAction(() => orderService.cancelOrder(order.orderId));

    if (nextOrder) {
      setOrder(nextOrder);
    }
  };

  if (isLoading) {
    return <LoadingState />;
  }

  if (errorMessage || !order) {
    return <ErrorState message={errorMessage ?? MESSAGES.COMMON.UNKNOWN_ERROR} onRetry={loadOrder} />;
  }

  const canCancel = order.status === ORDER_STATUS.CREATED;

  return (
    <section className="grid gap-6">
      <PageHeader
        description={`주문일시 ${formatDateTime(order.orderedAt)}`}
        title={`주문 #${order.orderId}`}
      />
      {actionErrorMessage ? <ErrorState message={actionErrorMessage} /> : null}
      <div className="grid gap-6 lg:grid-cols-[1fr_320px]">
        <div className="grid gap-4">
          <div className="flex flex-wrap items-center gap-3 rounded-card border border-line bg-surface p-4">
            <StatusBadge label={order.status} tone={order.status === ORDER_STATUS.CANCELLED ? "error" : "warning"} />
            <span className="text-sm text-ink-soft">결제 전 주문 상태를 확인하세요.</span>
          </div>
          <OrderItemTable items={order.items} />
        </div>
        <aside className="grid h-fit gap-4 rounded-card border border-line bg-surface p-5">
          <h2 className="text-lg font-bold">결제 직전</h2>
          <div className="grid gap-3 text-sm">
            <div className="flex justify-between">
              <span className="text-ink-soft">상품 금액</span>
              <PriceText amount={order.totalProductPrice} className="font-semibold" />
            </div>
            <div className="flex justify-between">
              <span className="text-ink-soft">배송비</span>
              <PriceText amount={order.shippingFee} className="font-semibold" />
            </div>
            <div className="flex justify-between border-t border-line pt-3">
              <span className="font-semibold">총 결제 금액</span>
              <PriceText amount={order.totalPrice} className="text-lg font-bold" />
            </div>
          </div>
          <Button disabled icon={<CreditCard size={16} />}>
            결제하기
          </Button>
          <p className="text-xs leading-5 text-ink-soft">{MESSAGES.ORDER.PAYMENT_PENDING}</p>
          {canCancel ? (
            <Button disabled={isActionLoading} onClick={handleCancel} variant="secondary">
              주문 취소
            </Button>
          ) : null}
        </aside>
      </div>
    </section>
  );
};
