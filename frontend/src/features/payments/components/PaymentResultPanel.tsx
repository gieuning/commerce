import type { ReactNode } from "react";
import { PriceText } from "@/components/PriceText";
import { StatusBadge } from "@/components/StatusBadge";
import { PAYMENT_STATUS_LABELS, PAYMENT_STATUS_TONES } from "@/constants/statusLabels";
import type { Payment } from "@/types/payment";

interface PaymentResultPanelProps {
  actions?: ReactNode;
  description: string;
  payment?: Payment;
  title: string;
}

export const PaymentResultPanel = ({
  actions,
  description,
  payment,
  title,
}: PaymentResultPanelProps) => (
  <section className="mx-auto grid w-full max-w-2xl gap-5 rounded-card border border-line bg-surface p-6">
    <div className="grid gap-2">
      <h1 className="text-2xl font-bold">{title}</h1>
      <p className="text-sm leading-6 text-ink-soft">{description}</p>
    </div>
    {payment ? (
      <div className="grid gap-3 rounded-card bg-background p-4 text-sm">
        <div className="flex justify-between">
          <span className="text-ink-soft">주문 번호</span>
          <span className="font-semibold">{payment.orderId}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-ink-soft">결제 번호</span>
          <span className="font-semibold">{payment.paymentId}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-ink-soft">결제 상태</span>
          <StatusBadge
            label={PAYMENT_STATUS_LABELS[payment.status]}
            tone={PAYMENT_STATUS_TONES[payment.status]}
          />
        </div>
        <div className="flex justify-between">
          <span className="text-ink-soft">결제 금액</span>
          <PriceText amount={payment.amount} className="font-semibold" />
        </div>
      </div>
    ) : null}
    {actions ? <div className="flex flex-wrap gap-2">{actions}</div> : null}
  </section>
);
