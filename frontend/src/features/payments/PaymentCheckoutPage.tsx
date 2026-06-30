import { CreditCard } from "lucide-react";
import type { ChangeEvent } from "react";
import { useCallback, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { Button } from "@/components/Button";
import { ErrorState } from "@/components/ErrorState";
import { LoadingState } from "@/components/LoadingState";
import { PageHeader } from "@/components/PageHeader";
import { Select } from "@/components/Select";
import { MESSAGES } from "@/constants/messages";
import {
  PAYMENT_METHOD_LABELS,
  PAYMENT_METHOD_OPTIONS,
  PAYMENT_REDIRECT_BASE_URL,
  TOSS_PAYMENTS_CLIENT_KEY,
} from "@/constants/payment";
import { ROUTES } from "@/constants/routes";
import { PaymentAmountSummary } from "@/features/payments/components/PaymentAmountSummary";
import { useApiErrorHandler } from "@/hooks/useApiErrorHandler";
import { orderService } from "@/services/orderService";
import { paymentService } from "@/services/paymentService";
import { tossPaymentService } from "@/services/tossPaymentService";
import { ORDER_STATUS, type Order } from "@/types/order";
import { PAYMENT_METHOD, type PaymentMethod } from "@/types/payment";
import { formatDateTime } from "@/utils/formatDateTime";
import { parsePaymentAmount } from "@/utils/parsePaymentAmount";
import { parseRouteNumber } from "@/utils/parseRouteNumber";

const createRedirectUrl = (routePath: string): string => `${PAYMENT_REDIRECT_BASE_URL}${routePath}`;

const createOrderName = (order: Order): string => {
  const firstOrderItem = order.items[0];

  if (!firstOrderItem) {
    return `주문 #${order.orderId}`;
  }

  const extraItemCount = order.items.length - 1;
  return extraItemCount > 0
    ? `${firstOrderItem.productName} 외 ${extraItemCount}건`
    : firstOrderItem.productName;
};

const isPaymentMethod = (method: string): method is PaymentMethod =>
  Object.values(PAYMENT_METHOD).includes(method as PaymentMethod);

export const PaymentCheckoutPage = () => {
  const handleApiError = useApiErrorHandler();
  const params = useParams();
  const orderId = parseRouteNumber(params.orderId);
  const [order, setOrder] = useState<Order | null>(null);
  const [selectedMethod, setSelectedMethod] = useState<PaymentMethod>(PAYMENT_METHOD.CARD);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

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
      .catch((error: unknown) => setErrorMessage(handleApiError(error)))
      .finally(() => setIsLoading(false));
  }, [handleApiError, orderId]);

  useEffect(() => {
    loadOrder();
  }, [loadOrder]);

  const handleMethodChange = (event: ChangeEvent<HTMLSelectElement>) => {
    if (isPaymentMethod(event.target.value)) {
      setSelectedMethod(event.target.value);
    }
  };

  const handleRequestPayment = async () => {
    if (!order || isSubmitting) {
      return;
    }

    if (!TOSS_PAYMENTS_CLIENT_KEY) {
      setErrorMessage(MESSAGES.PAYMENT.CLIENT_KEY_MISSING);
      return;
    }

    setIsSubmitting(true);
    setErrorMessage(null);

    try {
      const requestedPayment = await paymentService.requestPayment({
        method: selectedMethod,
        orderId: order.orderId,
      });

      await tossPaymentService.requestPayment({
        amount: parsePaymentAmount(requestedPayment.amount),
        clientKey: TOSS_PAYMENTS_CLIENT_KEY,
        failUrl: createRedirectUrl(ROUTES.PAYMENT_FAIL),
        method: requestedPayment.method,
        merchantOrderId: requestedPayment.merchantOrderId,
        orderName: createOrderName(order),
        successUrl: createRedirectUrl(ROUTES.PAYMENT_SUCCESS),
      });
    } catch (error: unknown) {
      setErrorMessage(handleApiError(error));
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return <LoadingState />;
  }

  if (errorMessage && !order) {
    return <ErrorState message={errorMessage} onRetry={loadOrder} />;
  }

  if (!order) {
    return <ErrorState message={MESSAGES.COMMON.UNKNOWN_ERROR} onRetry={loadOrder} />;
  }

  const canRequestPayment = order.status === ORDER_STATUS.CREATED;

  return (
    <section className="grid gap-6">
      <PageHeader
        description={`주문일시 ${formatDateTime(order.orderedAt)}`}
        title={`주문 #${order.orderId} 결제`}
      />
      {errorMessage ? <ErrorState message={errorMessage} /> : null}
      <div className="grid gap-6 lg:grid-cols-[1fr_320px]">
        <div className="grid gap-4 rounded-card border border-line bg-surface p-5">
          <div className="grid gap-2">
            <h2 className="text-lg font-bold">결제 정보</h2>
            <p className="text-sm leading-6 text-ink-soft">{MESSAGES.PAYMENT.CHECKOUT_READY}</p>
          </div>
          <Select
            disabled={!canRequestPayment || isSubmitting}
            label={MESSAGES.PAYMENT.METHOD_LABEL}
            onChange={handleMethodChange}
            value={selectedMethod}
          >
            {PAYMENT_METHOD_OPTIONS.map((paymentMethod) => (
              <option key={paymentMethod.value} value={paymentMethod.value}>
                {paymentMethod.label}
              </option>
            ))}
          </Select>
          <div className="rounded-card bg-background p-4 text-sm text-ink-soft">
            선택한 결제 수단: {PAYMENT_METHOD_LABELS[selectedMethod]}
          </div>
        </div>
        <aside className="grid h-fit gap-4 rounded-card border border-line bg-surface p-5">
          <h2 className="text-lg font-bold">결제 금액</h2>
          <PaymentAmountSummary order={order} />
          <Button
            disabled={!canRequestPayment || isSubmitting}
            icon={<CreditCard size={16} />}
            onClick={handleRequestPayment}
          >
            {isSubmitting ? "결제창 여는 중" : "결제하기"}
          </Button>
          {!canRequestPayment ? (
            <p className="text-xs leading-5 text-error">{MESSAGES.ORDER.PAYMENT_UNAVAILABLE}</p>
          ) : null}
        </aside>
      </div>
    </section>
  );
};
