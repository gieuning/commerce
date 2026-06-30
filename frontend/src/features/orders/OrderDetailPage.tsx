import { CreditCard } from "lucide-react";
import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { Button } from "@/components/Button";
import { ErrorState } from "@/components/ErrorState";
import { LoadingState } from "@/components/LoadingState";
import { PageHeader } from "@/components/PageHeader";
import { StatusBadge } from "@/components/StatusBadge";
import { MESSAGES } from "@/constants/messages";
import { ROUTES } from "@/constants/routes";
import { ORDER_STATUS_LABELS, ORDER_STATUS_TONES } from "@/constants/statusLabels";
import { PaymentAmountSummary } from "@/features/payments/components/PaymentAmountSummary";
import { useAsyncAction } from "@/hooks/useAsyncAction";
import { orderService } from "@/services/orderService";
import { ORDER_STATUS, type Order } from "@/types/order";
import { formatDateTime } from "@/utils/formatDateTime";
import { getApiErrorMessage } from "@/utils/getApiErrorMessage";
import { parseRouteNumber } from "@/utils/parseRouteNumber";
import { OrderItemTable } from "@/features/orders/components/OrderItemTable";

export const OrderDetailPage = () => {
  const navigate = useNavigate();
  const params = useParams();
  const orderId = parseRouteNumber(params.orderId);
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
      .catch((error: unknown) => setErrorMessage(getApiErrorMessage(error)))
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
  const canRequestPayment = order.status === ORDER_STATUS.CREATED;

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
            <StatusBadge
              label={ORDER_STATUS_LABELS[order.status]}
              tone={ORDER_STATUS_TONES[order.status]}
            />
            <span className="text-sm text-ink-soft">결제 전 주문 상태를 확인하세요.</span>
          </div>
          <OrderItemTable items={order.items} />
        </div>
        <aside className="grid h-fit gap-4 rounded-card border border-line bg-surface p-5">
          <h2 className="text-lg font-bold">결제 직전</h2>
          <PaymentAmountSummary order={order} />
          <Button
            disabled={!canRequestPayment}
            icon={<CreditCard size={16} />}
            onClick={() => navigate(ROUTES.PAYMENT_CHECKOUT(order.orderId))}
          >
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
