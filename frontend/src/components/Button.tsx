import type { ButtonHTMLAttributes, ReactNode } from "react";
import { cn } from "@/utils/cn";

type ButtonVariant = "primary" | "secondary" | "ghost" | "danger";
type ButtonSize = "sm" | "md" | "lg";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  size?: ButtonSize;
  icon?: ReactNode;
}

const VARIANT_CLASSES: Record<ButtonVariant, string> = {
  primary: "bg-primary text-white hover:bg-primary-hover focus-visible:ring-primary",
  secondary: "border border-line bg-surface text-ink hover:border-primary focus-visible:ring-primary",
  ghost: "bg-transparent text-ink-soft hover:bg-line/60 focus-visible:ring-primary",
  danger: "bg-error text-white hover:bg-red-600 focus-visible:ring-error",
};

const SIZE_CLASSES: Record<ButtonSize, string> = {
  sm: "h-9 px-3 text-sm",
  md: "h-10 px-4 text-sm",
  lg: "h-12 px-5 text-base",
};

export const Button = ({
  children,
  className,
  disabled,
  icon,
  size = "md",
  type = "button",
  variant = "primary",
  ...buttonProps
}: ButtonProps) => (
  <button
    className={cn(
      "inline-flex items-center justify-center gap-2 rounded-btn font-semibold transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50",
      VARIANT_CLASSES[variant],
      SIZE_CLASSES[size],
      className,
    )}
    disabled={disabled}
    type={type}
    {...buttonProps}
  >
    {icon}
    {children}
  </button>
);
