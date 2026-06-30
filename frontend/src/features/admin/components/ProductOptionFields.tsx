import { Plus, Trash2 } from "lucide-react";
import { Button } from "@/components/Button";
import { Input } from "@/components/Input";
import type {
  EditableOptionCombination,
  EditableOptionGroup,
} from "@/features/admin/hooks/useProductOptions";

interface ProductOptionFieldsProps {
  combinations: EditableOptionCombination[];
  groups: EditableOptionGroup[];
  onAddGroup: () => void;
  onGenerateCombinations: () => void;
  onRemoveGroup: (groupId: string) => void;
  onUpdateCombination: (
    combinationId: string,
    fieldName: "additionalPrice" | "stock",
    value: string,
  ) => void;
  onUpdateGroup: (groupId: string, fieldName: "name" | "valuesText", value: string) => void;
}

export const ProductOptionFields = ({
  combinations,
  groups,
  onAddGroup,
  onGenerateCombinations,
  onRemoveGroup,
  onUpdateCombination,
  onUpdateGroup,
}: ProductOptionFieldsProps) => (
  <section className="grid gap-4 rounded-card border border-line bg-background p-4">
    <div className="flex flex-wrap items-center justify-between gap-2">
      <h2 className="text-base font-bold">옵션 구성</h2>
      <Button icon={<Plus size={16} />} onClick={onAddGroup} size="sm" variant="secondary">
        그룹 추가
      </Button>
    </div>
    <div className="grid gap-3">
      {groups.map((group) => (
        <div className="grid gap-3 rounded-card border border-line bg-surface p-3" key={group.id}>
          <div className="grid gap-3 md:grid-cols-[1fr_2fr_auto]">
            <Input
              label="옵션 그룹명"
              onChange={(event) => onUpdateGroup(group.id, "name", event.target.value)}
              placeholder="예: 색상"
              required
              value={group.name}
            />
            <Input
              label="옵션값"
              onChange={(event) => onUpdateGroup(group.id, "valuesText", event.target.value)}
              placeholder="예: 블랙, 화이트"
              required
              value={group.valuesText}
            />
            <div className="flex items-end">
              <Button
                aria-label="옵션 그룹 삭제"
                icon={<Trash2 size={16} />}
                onClick={() => onRemoveGroup(group.id)}
                size="sm"
                variant="ghost"
              />
            </div>
          </div>
        </div>
      ))}
    </div>
    <Button onClick={onGenerateCombinations} variant="secondary">
      옵션 조합 생성
    </Button>
    {combinations.length > 0 ? (
      <div className="overflow-hidden rounded-card border border-line bg-surface">
        <table className="w-full text-left text-sm">
          <thead className="bg-line/50 text-ink-soft">
            <tr>
              <th className="px-3 py-2">옵션 조합</th>
              <th className="px-3 py-2">추가금</th>
              <th className="px-3 py-2">재고</th>
            </tr>
          </thead>
          <tbody>
            {combinations.map((combination) => (
              <tr className="border-t border-line" key={combination.id}>
                <td className="px-3 py-2 font-semibold">{combination.optionValues.join(" / ")}</td>
                <td className="px-3 py-2">
                  <Input
                    aria-label={`${combination.optionValues.join(" / ")} 추가금`}
                    label="추가금"
                    min="0"
                    onChange={(event) =>
                      onUpdateCombination(combination.id, "additionalPrice", event.target.value)
                    }
                    required
                    type="number"
                    value={combination.additionalPrice}
                  />
                </td>
                <td className="px-3 py-2">
                  <Input
                    aria-label={`${combination.optionValues.join(" / ")} 재고`}
                    label="재고"
                    min="0"
                    onChange={(event) => onUpdateCombination(combination.id, "stock", event.target.value)}
                    required
                    type="number"
                    value={combination.stock}
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    ) : null}
  </section>
);
