import { Link } from "react-router-dom";
import { EmptyState } from "@/components/EmptyState";
import { ErrorState } from "@/components/ErrorState";
import { LoadingState } from "@/components/LoadingState";
import { PageHeader } from "@/components/PageHeader";
import { Pagination } from "@/components/Pagination";
import { PriceText } from "@/components/PriceText";
import { StatusBadge } from "@/components/StatusBadge";
import { MESSAGES } from "@/constants/messages";
import { ROUTES } from "@/constants/routes";
import { ORDER_STATUS_LABELS, ORDER_STATUS_TONES } from "@/constants/statusLabels";
import { orderService } from "@/services/orderService";
import type { PageResult } from "@/types/api";
import type { Order } from "@/types/order";
import { formatDateTime } from "@/utils/formatDateTime";
import { getApiErrorMessage } from "@/utils/getApiErrorMessage";
import { useCallback, useEffect, useState } from "react";

export const OrderListPage = () => {
  const [pageNumber, setPageNumber] = useState(0);
  const [orderPage, setOrderPage] = useState<PageResult<Order> | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const loadOrders = useCallback((isActive: () => boolean = () => true) => {
    setIsLoading(true);
    setErrorMessage(null);

    orderService
      .getOrders({ page: pageNumber })
      .then((nextOrderPage) => {
        if (isActive()) {
          setOrderPage(nextOrderPage);
        }
      })
      .catch((error: unknown) => {
        if (isActive()) {
          setErrorMessage(getApiErrorMessage(error));
        }
      })
      .finally(() => {
        if (isActive()) {
          setIsLoading(false);
        }
      });
  }, [pageNumber]);

  useEffect(() => {
    let isMounted = true;
    loadOrders(() => isMounted);

    return () => {
      isMounted = false;
    };
  }, [loadOrders]);

  return (
    <section className="grid gap-6">
      <PageHeader description="내 주문 내역과 결제 전 상태를 확인합니다." title="주문" />
      {isLoading ? <LoadingState /> : null}
      {errorMessage ? <ErrorState message={errorMessage} onRetry={() => loadOrders()} /> : null}
      {!isLoading && !errorMessage && orderPage?.content.length === 0 ? (
        <EmptyState message={MESSAGES.ORDER.EMPTY} />
      ) : null}
      {!isLoading && !errorMessage && orderPage && orderPage.content.length > 0 ? (
        <>
          <div className="grid gap-3">
            {orderPage.content.map((order) => (
              <Link
                className="grid gap-3 rounded-card border border-line bg-surface p-4 transition hover:border-primary md:grid-cols-[1fr_auto]"
                key={order.orderId}
                to={ROUTES.ORDER_DETAIL(order.orderId)}
              >
                <div>
                  <div className="flex flex-wrap items-center gap-2">
                    <h2 className="font-semibold">주문 #{order.orderId}</h2>
                    <StatusBadge
                      label={ORDER_STATUS_LABELS[order.status]}
                      tone={ORDER_STATUS_TONES[order.status]}
                    />
                  </div>
                  <p className="mt-2 text-sm text-ink-soft">{formatDateTime(order.orderedAt)}</p>
                </div>
                <PriceText amount={order.totalPrice} className="text-lg font-bold" />
              </Link>
            ))}
          </div>
          <Pagination page={orderPage} onPageChange={setPageNumber} />
        </>
      ) : null}
    </section>
  );
};
