 import { PriceText } from "@/components/PriceText";
import type { Order } from "@/types/order";

interface PaymentAmountSummaryProps {
  order: Order;
}

export const PaymentAmountSummary = ({ order }: PaymentAmountSummaryProps) => (
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
);
