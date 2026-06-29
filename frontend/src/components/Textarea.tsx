import { useId, type TextareaHTMLAttributes } from "react";
import { cn } from "@/utils/cn";

interface TextareaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label: string;
}

export const Textarea = ({ className, id, label, ...textareaProps }: TextareaProps) => {
  const generatedId = useId();
  const textareaId = id ?? textareaProps.name ?? generatedId;

  return (
    <label className="grid gap-2 text-sm font-medium text-ink" htmlFor={textareaId}>
      <span>{label}</span>
      <textarea
        className={cn(
          "min-h-28 rounded-btn border border-line bg-surface px-3 py-3 text-sm outline-none transition placeholder:text-neutral focus:border-primary focus:ring-2 focus:ring-primary/20",
          className,
        )}
        id={textareaId}
        {...textareaProps}
      />
    </label>
  );
};
