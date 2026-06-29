import { MESSAGES } from "@/constants/messages";

interface LoadingStateProps {
  message?: string;
}

export const LoadingState = ({ message = MESSAGES.COMMON.LOADING }: LoadingStateProps) => (
  <div
    className="flex min-h-48 items-center justify-center rounded-card border border-line bg-surface p-8 text-sm text-ink-soft"
    role="status"
  >
    {message}
  </div>
);
