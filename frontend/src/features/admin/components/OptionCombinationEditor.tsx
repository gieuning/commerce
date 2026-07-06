import { useState } from "react";
import { Button } from "@/components/Button";
import { ErrorState } from "@/components/ErrorState";
import { Input } from "@/components/Input";
import { Select } from "@/components/Select";
import { MESSAGES } from "@/constants/messages";
import { PRODUCT_STATUS_LABELS } from "@/constants/statusLabels";
import { productService } from "@/services/productService";
import {
  PRODUCT_STATUS,
  type OptionCombination,
  type ProductDetail,
  type ProductStatus,
} from "@/types/product";
import { parseNonNegativeNumberField } from "@/utils/parseNumberField";

interface OptionCombinationEditorProps {
  product: ProductDetail;
  onUpdated: (product: ProductDetail) => void;
}

interface EditableRow {
  additionalPrice: string;
  stock: string;
  status: ProductStatus;
}

const toEditableRow = (combination: OptionCombination): EditableRow => ({
  additionalPrice: combination.additionalPrice,
  stock: String(combination.stock),
  status: combination.status,
});

const buildRows = (combinations: OptionCombination[]): Record<number, EditableRow> =>
  Object.fromEntries(combinations.map((combination) => [combination.id, toEditableRow(combination)]));

export const OptionCombinationEditor = ({ product, onUpdated }: OptionCombinationEditorProps) => {
  const [rows, setRows] = useState<Record<number, EditableRow>>(() => buildRows(product.combinations));
  const [savingId, setSavingId] = useState<number | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const updateRow = (combinationId: number, patch: Partial<EditableRow>) => {
    setRows((currentRows) => ({
      ...currentRows,
      [combinationId]: { ...currentRows[combinationId], ...patch },
    }));
  };

  const handleSave = async (combinationId: number) => {
    const row = rows[combinationId];
    const additionalPrice = parseNonNegativeNumberField(row.additionalPrice);
    const stock = parseNonNegativeNumberField(row.stock);

    if (additionalPrice === null || stock === null) {
      setErrorMessage(MESSAGES.ADMIN_PRODUCT.INVALID_NUMBER);
      return;
    }

    setSavingId(combinationId);
    setErrorMessage(null);

    try {
      const updatedProduct = await productService.updateCombination(product.id, combinationId, {
        additionalPrice,
        stock,
        status: row.status,
      });
      onUpdated(updatedProduct);
    } catch {
      setErrorMessage(MESSAGES.COMMON.UNKNOWN_ERROR);
    } finally {
      setSavingId(null);
    }
  };

  return (
    <section className="grid gap-4 rounded-card border border-line bg-surface p-5">
      <h2 className="text-lg font-bold">{MESSAGES.ADMIN_PRODUCT.COMBINATION_TITLE}</h2>

      <div className="grid gap-1 text-sm text-ink-soft">
        <span className="font-semibold text-ink">{MESSAGES.ADMIN_PRODUCT.OPTION_GROUPS_TITLE}</span>
        {product.optionGroups.map((group) => (
          <span key={group.name}>
            {group.name}: {group.values.join(", ")}
          </span>
        ))}
      </div>

      {errorMessage ? <ErrorState message={errorMessage} /> : null}

      {product.combinations.length === 0 ? (
        <p className="text-sm text-ink-soft">{MESSAGES.ADMIN_PRODUCT.COMBINATION_EMPTY}</p>
      ) : (
        <div className="grid gap-3">
          {product.combinations.map((combination) => {
            const row = rows[combination.id];
            const isSaving = savingId === combination.id;

            return (
              <div
                className="grid items-end gap-3 rounded-card border border-line bg-background p-3 md:grid-cols-[1.5fr_1fr_1fr_1.2fr_auto]"
                key={combination.id}
              >
                <span className="text-sm font-semibold">{combination.optionValues.join(" / ")}</span>
                <Input
                  label="추가금"
                  min="0"
                  onChange={(event) => updateRow(combination.id, { additionalPrice: event.target.value })}
                  type="number"
                  value={row.additionalPrice}
                />
                <Input
                  label="재고"
                  min="0"
                  onChange={(event) => updateRow(combination.id, { stock: event.target.value })}
                  type="number"
                  value={row.stock}
                />
                <Select
                  label="상태"
                  onChange={(event) =>
                    updateRow(combination.id, { status: event.target.value as ProductStatus })
                  }
                  value={row.status}
                >
                  {Object.values(PRODUCT_STATUS).map((status) => (
                    <option key={status} value={status}>
                      {PRODUCT_STATUS_LABELS[status]}
                    </option>
                  ))}
                </Select>
                <Button
                  disabled={isSaving}
                  onClick={() => handleSave(combination.id)}
                  size="sm"
                  type="button"
                  variant="secondary"
                >
                  {isSaving ? "저장 중" : "저장"}
                </Button>
              </div>
            );
          })}
        </div>
      )}
    </section>
  );
};
