import { formatCurrency } from "@/utils/formatCurrency";

interface PriceTextProps {
  amount: number | string;
  className?: string;
}

export const PriceText = ({ amount, className }: PriceTextProps) => (
  <span className={className}>{formatCurrency(amount)}</span>
);
