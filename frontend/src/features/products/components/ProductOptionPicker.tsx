import { Select } from "@/components/Select";
import { PRODUCT_STATUS_LABELS } from "@/constants/statusLabels";
import { PRODUCT_STATUS, type OptionCombination } from "@/types/product";
import { formatCurrency } from "@/utils/formatCurrency";

interface ProductOptionPickerProps {
  combinations: OptionCombination[];
  selectedCombinationId: number | null;
  onChange: (combinationId: number | null) => void;
}

// 옵션 추가금은 합산값이 아니라 "(+1,000원)"처럼 더해지는 금액으로 보여준다(추가금 0이면 생략).
const buildOptionLabel = (combination: OptionCombination): string => {
  const values = combination.optionValues.join(" / ");
  const additionalPrice = Number(combination.additionalPrice);
  const priceLabel = additionalPrice > 0 ? ` (+${formatCurrency(additionalPrice)})` : "";
  const stateLabel = PRODUCT_STATUS_LABELS[combination.status];

  return `${values}${priceLabel} · 재고 ${combination.stock} · ${stateLabel}`;
};

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
        {buildOptionLabel(combination)}
      </option>
    ))}
  </Select>
);
