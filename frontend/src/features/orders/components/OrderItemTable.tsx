import { PriceText } from "@/components/PriceText";
import type { OrderItem } from "@/types/order";

interface OrderItemTableProps {
  items: OrderItem[];
}

export const OrderItemTable = ({ items }: OrderItemTableProps) => (
  <div className="overflow-hidden rounded-card border border-line bg-surface">
    <table className="w-full border-collapse text-left text-sm">
      <thead className="bg-line/50 text-ink-soft">
        <tr>
          <th className="px-4 py-3 font-semibold">상품</th>
          <th className="px-4 py-3 font-semibold">옵션</th>
          <th className="px-4 py-3 font-semibold">수량</th>
          <th className="px-4 py-3 text-right font-semibold">금액</th>
        </tr>
      </thead>
      <tbody>
        {items.map((item) => (
          <tr className="border-t border-line" key={item.itemId}>
            <td className="px-4 py-3 font-medium">{item.productName}</td>
            <td className="px-4 py-3 text-ink-soft">{item.optionValues ?? "-"}</td>
            <td className="px-4 py-3">{item.quantity}</td>
            <td className="px-4 py-3 text-right">
              <PriceText amount={item.subtotal} className="font-semibold" />
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  </div>
);
