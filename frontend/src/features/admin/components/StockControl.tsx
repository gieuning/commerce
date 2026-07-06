import { type FormEvent, useState } from "react";
import { Button } from "@/components/Button";
import { Input } from "@/components/Input";
import { MESSAGES } from "@/constants/messages";

interface StockControlProps {
  initialStock: number;
  isSubmitting: boolean;
  onSubmit: (stock: number) => void;
}

export const StockControl = ({ initialStock, isSubmitting, onSubmit }: StockControlProps) => {
  const [stock, setStock] = useState(String(initialStock));
  const [stockError, setStockError] = useState<string | undefined>(undefined);

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (stock.trim() === "") {
      setStockError(MESSAGES.ADMIN_PRODUCT.STOCK_REQUIRED);
      return;
    }

    onSubmit(Number(stock));
  };

  return (
    <form className="grid gap-4 rounded-card border border-line bg-surface p-5" noValidate onSubmit={handleSubmit}>
      <h2 className="text-lg font-bold">재고 수정</h2>
      <Input
        errorMessage={stockError}
        label="재고"
        min="0"
        name="stock"
        onChange={(event) => {
          setStock(event.target.value);
          setStockError(undefined);
        }}
        required
        type="number"
        value={stock}
      />
      <Button disabled={isSubmitting} type="submit" variant="secondary">
        재고 저장
      </Button>
    </form>
  );
};
