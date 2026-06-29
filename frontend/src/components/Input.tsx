import { useId, type InputHTMLAttributes } from "react";
import { cn } from "@/utils/cn";

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  errorMessage?: string;
}

export const Input = ({ className, errorMessage, id, label, ...inputProps }: InputProps) => {
  const generatedId = useId();
  const inputId = id ?? inputProps.name ?? generatedId;

  return (
    <label className="grid gap-2 text-sm font-medium text-ink" htmlFor={inputId}>
      <span>{label}</span>
      <input
        className={cn(
          "h-11 rounded-btn border border-line bg-surface px-3 text-sm outline-none transition placeholder:text-neutral focus:border-primary focus:ring-2 focus:ring-primary/20",
          errorMessage ? "border-error focus:border-error focus:ring-error/20" : undefined,
          className,
        )}
        id={inputId}
        {...inputProps}
      />
      {errorMessage ? <span className="text-xs font-normal text-error">{errorMessage}</span> : null}
    </label>
  );
};
