import { useId, type SelectHTMLAttributes } from "react";
import { cn } from "@/utils/cn";

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label: string;
}

export const Select = ({ children, className, id, label, ...selectProps }: SelectProps) => {
  const generatedId = useId();
  const selectId = id ?? selectProps.name ?? generatedId;

  return (
    <label className="grid gap-2 text-sm font-medium text-ink" htmlFor={selectId}>
      <span>{label}</span>
      <select
        className={cn(
          "h-11 rounded-btn border border-line bg-surface px-3 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/20",
          className,
        )}
        id={selectId}
        {...selectProps}
      >
        {children}
      </select>
    </label>
  );
};
