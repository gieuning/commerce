import { CheckCircle2 } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Button } from "@/components/Button";
import { ErrorState } from "@/components/ErrorState";
import { LoadingState } from "@/components/LoadingState";
import { MESSAGES } from "@/constants/messages";
import { ROUTES } from "@/constants/routes";
import { PaymentResultPanel } from "@/features/payments/components/PaymentResultPanel";
import { useApiErrorHandler } from "@/hooks/useApiErrorHandler";
import { paymentService } from "@/services/paymentService";
import type { Payment } from "@/types/payment";

const createConfirmRequest = (searchParams: URLSearchParams) => {
  const paymentKey = searchParams.get("paymentKey");
  const merchantOrderId = searchParams.get("orderId");
  const amount = searchParams.get("amount");

  if (!paymentKey || !merchantOrderId || !amount) {
    return null;
  }

  return {
    amount,
    merchantOrderId,
    paymentKey,
  };
};

export const PaymentSuccessPage = () => {
  const navigate = useNavigate();
  const handleApiError = useApiErrorHandler();
  const [searchParams] = useSearchParams();
  const hasConfirmedRef = useRef(false);
  const [payment, setPayment] = useState<Payment | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (hasConfirmedRef.current) {
      return;
    }

    hasConfirmedRef.current = true;
    const confirmRequest = createConfirmRequest(searchParams);

    if (!confirmRequest) {
      setErrorMessage(MESSAGES.PAYMENT.INVALID_CALLBACK);
      setIsLoading(false);
      return;
    }

    paymentService
      .confirmPayment(confirmRequest)
      .then(setPayment)
      .catch((error: unknown) => setErrorMessage(handleApiError(error)))
      .finally(() => setIsLoading(false));
  }, [handleApiError, searchParams]);

  if (isLoading) {
    return <LoadingState message={MESSAGES.PAYMENT.CONFIRMING} />;
  }

  if (errorMessage) {
    return (
      <section className="mx-auto grid w-full max-w-2xl gap-4">
        <ErrorState message={errorMessage} />
        <div className="flex flex-wrap gap-2">
          <Button onClick={() => navigate(ROUTES.ORDERS)} variant="secondary">
            주문 목록
          </Button>
          <Button onClick={() => navigate(ROUTES.LOGIN)} variant="secondary">
            로그인
          </Button>
        </div>
      </section>
    );
  }

  if (!payment) {
    return <ErrorState message={MESSAGES.COMMON.UNKNOWN_ERROR} />;
  }

  return (
    <PaymentResultPanel
      actions={
        <>
          <Button icon={<CheckCircle2 size={16} />} onClick={() => navigate(ROUTES.ORDER_DETAIL(payment.orderId))}>
            주문 상세
          </Button>
          <Button onClick={() => navigate(ROUTES.ORDERS)} variant="secondary">
            주문 목록
          </Button>
        </>
      }
      description="승인된 결제 결과가 주문에 반영되었습니다."
      payment={payment}
      title={MESSAGES.PAYMENT.SUCCESS}
    />
  );
};
