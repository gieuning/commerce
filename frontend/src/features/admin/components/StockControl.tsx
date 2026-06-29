import { type FormEvent, useState } from "react";
import { Button } from "@/components/Button";
import { Input } from "@/components/Input";

interface StockControlProps {
  initialStock: number;
  isSubmitting: boolean;
  onSubmit: (stock: number) => void;
}

export const StockControl = ({ initialStock, isSubmitting, onSubmit }: StockControlProps) => {
  const [stock, setStock] = useState(initialStock);

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    onSubmit(stock);
  };

  return (
    <form className="grid gap-4 rounded-card border border-line bg-surface p-5" onSubmit={handleSubmit}>
      <h2 className="text-lg font-bold">재고 수정</h2>
      <Input
        label="재고"
        min="0"
        name="stock"
        onChange={(event) => setStock(Number(event.target.value))}
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
