import { Select } from "@/components/Select";
import { PRODUCT_STATUS_LABELS } from "@/constants/statusLabels";
import { PRODUCT_STATUS, type OptionCombination } from "@/types/product";
import { formatCurrency } from "@/utils/formatCurrency";

interface ProductOptionPickerProps {
  combinations: OptionCombination[];
  selectedCombinationId: number | null;
  onChange: (combinationId: number | null) => void;
}

export const ProductOptionPicker = ({
  combinations,
  onChange,
  selectedCombinationId,
}: ProductOptionPickerProps) => (
  <Select
    label="옵션"
    onChange={(event) =>
      onChange(event.target.value === "" ? null : Number(event.target.value))
    }
    required
    value={selectedCombinationId ?? ""}
  >
    <option value="">옵션을 선택하세요</option>
    {combinations.map((combination) => (
      <option
        disabled={combination.status !== PRODUCT_STATUS.FOR_SALE || combination.stock <= 0}
        key={combination.id}
        value={combination.id}
      >
        {combination.optionValues.join(" / ")} · {formatCurrency(combination.finalPrice)} · 재고{" "}
        {combination.stock} · {PRODUCT_STATUS_LABELS[combination.status]}
      </option>
    ))}
  </Select>
);
