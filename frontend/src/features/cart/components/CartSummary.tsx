import { Button } from "@/components/Button";
import { PriceText } from "@/components/PriceText";
import type { Cart } from "@/types/cart";

interface CartSummaryProps {
  cart: Cart;
  isSubmitting: boolean;
  onCreateOrder: () => void;
}

export const CartSummary = ({ cart, isSubmitting, onCreateOrder }: CartSummaryProps) => {
  const hasUnavailableItems = cart.items.some((cartItem) => !cartItem.available);
  const hasItems = cart.items.length > 0;

  return (
    <aside className="grid h-fit gap-4 rounded-card border border-line bg-surface p-5">
      <h2 className="text-lg font-bold">주문 요약</h2>
      <div className="grid gap-3 text-sm">
        <div className="flex justify-between">
          <span className="text-ink-soft">총 수량</span>
          <strong>{cart.totalQuantity}개</strong>
        </div>
        <div className="flex justify-between">
          <span className="text-ink-soft">상품 금액</span>
          <PriceText amount={cart.totalPrice} className="font-bold" />
        </div>
      </div>
      <Button disabled={!hasItems || hasUnavailableItems || isSubmitting} onClick={onCreateOrder}>
        {isSubmitting ? "주문 생성 중" : "주문 생성"}
      </Button>
      {hasUnavailableItems ? (
        <p className="text-xs leading-5 text-warning">주문할 수 없는 상품을 삭제하거나 수량을 조정해 주세요.</p>
      ) : null}
    </aside>
  );
};
