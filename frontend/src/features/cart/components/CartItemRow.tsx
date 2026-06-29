import { Trash2 } from "lucide-react";
import { Button } from "@/components/Button";
import { PriceText } from "@/components/PriceText";
import { QuantityStepper } from "@/components/QuantityStepper";
import { StatusBadge } from "@/components/StatusBadge";
import type { CartItem } from "@/types/cart";

interface CartItemRowProps {
  item: CartItem;
  disabled?: boolean;
  onQuantityChange: (cartItemId: number, quantity: number) => void;
  onRemove: (cartItemId: number) => void;
}

export const CartItemRow = ({ disabled = false, item, onQuantityChange, onRemove }: CartItemRowProps) => (
  <article className="grid gap-4 rounded-card border border-line bg-surface p-4 md:grid-cols-[96px_1fr_auto]">
    <div className="h-24 overflow-hidden rounded-btn bg-line">
      {item.imageUrl ? (
        <img alt={item.productName ?? "상품"} className="h-full w-full object-cover" src={item.imageUrl} />
      ) : (
        <div className="grid h-full place-items-center text-xs text-neutral">이미지 없음</div>
      )}
    </div>
    <div className="grid gap-2">
      <div className="flex flex-wrap items-center gap-2">
        <h2 className="text-base font-semibold">{item.productName ?? "삭제된 상품"}</h2>
        <StatusBadge label={item.available ? "주문 가능" : "확인 필요"} tone={item.available ? "success" : "warning"} />
      </div>
      {item.optionValues.length > 0 ? (
        <p className="text-sm text-ink-soft">{item.optionValues.join(" / ")}</p>
      ) : null}
      {item.unavailableReason ? (
        <p className="text-sm font-medium text-warning">{item.unavailableReason}</p>
      ) : null}
      <p className="text-sm text-ink-soft">재고 {item.stock}</p>
      <PriceText amount={item.unitPrice} className="text-sm font-semibold" />
    </div>
    <div className="grid content-between gap-4 md:justify-items-end">
      <QuantityStepper
        disabled={disabled}
        label="수량"
        max={Math.max(item.stock, 1)}
        onChange={(nextQuantity) => onQuantityChange(item.itemId, nextQuantity)}
        value={item.quantity}
      />
      <div className="flex items-center justify-between gap-4 md:grid md:justify-items-end">
        <PriceText amount={item.subtotal} className="text-lg font-bold" />
        <Button
          disabled={disabled}
          icon={<Trash2 size={16} />}
          onClick={() => onRemove(item.itemId)}
          variant="ghost"
        >
          삭제
        </Button>
      </div>
    </div>
  </article>
);
