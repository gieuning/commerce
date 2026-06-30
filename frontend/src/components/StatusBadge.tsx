import { cn } from "@/utils/cn";

export type StatusBadgeTone = "neutral" | "success" | "warning" | "error";

interface StatusBadgeProps {
  label: string;
  tone?: StatusBadgeTone;
}

const TONE_CLASSES: Record<StatusBadgeTone, string> = {
  neutral: "bg-line text-ink-soft",
  success: "bg-success/10 text-success",
  warning: "bg-warning/15 text-yellow-700",
  error: "bg-error/10 text-error",
};

export const StatusBadge = ({ label, tone = "neutral" }: StatusBadgeProps) => (
  <span
    className={cn(
      "inline-flex h-7 items-center rounded-full px-3 text-xs font-semibold",
      TONE_CLASSES[tone],
    )}
  >
    {label}
  </span>
);
