import { AlertTriangle } from "lucide-react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Button } from "@/components/Button";
import { MESSAGES } from "@/constants/messages";
import { ROUTES } from "@/constants/routes";

export const PaymentFailPage = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const failureCode = searchParams.get("code");
  const failureMessage = searchParams.get("message") ?? MESSAGES.PAYMENT.FAIL;

  return (
    <section className="mx-auto grid w-full max-w-2xl gap-5 rounded-card border border-line bg-surface p-6">
      <div className="flex items-start gap-3">
        <AlertTriangle className="mt-1 text-error" size={22} aria-hidden />
        <div className="grid gap-2">
          <h1 className="text-2xl font-bold">{MESSAGES.PAYMENT.FAIL}</h1>
          <p className="text-sm leading-6 text-ink-soft">{failureMessage}</p>
        </div>
      </div>
      {failureCode ? (
        <div className="rounded-card bg-background p-4 text-sm">
          <span className="text-ink-soft">실패 코드 </span>
          <span className="font-semibold">{failureCode}</span>
        </div>
      ) : null}
      <div className="flex flex-wrap gap-2">
        <Button onClick={() => navigate(ROUTES.ORDERS)} variant="secondary">
          주문 목록
        </Button>
      </div>
    </section>
  );
};
