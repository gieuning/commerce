import { Minus, Plus } from "lucide-react";
import { Button } from "@/components/Button";

interface QuantityStepperProps {
  value: number;
  min?: number;
  max?: number;
  disabled?: boolean;
  label: string;
  onChange: (nextQuantity: number) => void;
}

export const QuantityStepper = ({
  label,
  max,
  min = 1,
  disabled = false,
  onChange,
  value,
}: QuantityStepperProps) => {
  const canDecrease = !disabled && value > min;
  const canIncrease = !disabled && (max === undefined || value < max);

  return (
    <div className="grid gap-2">
      <span className="text-sm font-medium text-ink">{label}</span>
      <div className="inline-grid w-fit grid-cols-[40px_56px_40px] overflow-hidden rounded-btn border border-line bg-surface">
        <Button
          aria-label={`${label} 감소`}
          className="h-10 rounded-none border-0"
          disabled={!canDecrease}
          icon={<Minus size={16} />}
          onClick={() => onChange(value - 1)}
          variant="ghost"
        />
        <output className="flex h-10 items-center justify-center border-x border-line text-sm font-semibold">
          {value}
        </output>
        <Button
          aria-label={`${label} 증가`}
          className="h-10 rounded-none border-0"
          disabled={!canIncrease}
          icon={<Plus size={16} />}
          onClick={() => onChange(value + 1)}
          variant="ghost"
        />
      </div>
    </div>
  );
};
