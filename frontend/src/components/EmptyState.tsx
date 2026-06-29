import { MESSAGES } from "@/constants/messages";

interface EmptyStateProps {
  message?: string;
}

export const EmptyState = ({ message = MESSAGES.COMMON.EMPTY_RESULT }: EmptyStateProps) => (
  <div className="grid min-h-48 place-items-center rounded-card border border-dashed border-line bg-surface p-8 text-center text-sm text-ink-soft">
    {message}
  </div>
);
