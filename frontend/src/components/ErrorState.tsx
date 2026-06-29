import { AlertTriangle } from "lucide-react";
import { MESSAGES } from "@/constants/messages";
import { Button } from "@/components/Button";

interface ErrorStateProps {
  message?: string;
  onRetry?: () => void;
}

export const ErrorState = ({ message = MESSAGES.COMMON.UNKNOWN_ERROR, onRetry }: ErrorStateProps) => (
  <div
    className="grid min-h-48 place-items-center rounded-card border border-error/20 bg-error/5 p-8 text-center"
    role="alert"
  >
    <div>
      <AlertTriangle className="mx-auto text-error" size={28} />
      <p className="mt-3 text-sm font-medium text-error">{message}</p>
      {onRetry ? (
        <Button className="mt-5" onClick={onRetry} variant="secondary">
          {MESSAGES.COMMON.RETRY}
        </Button>
      ) : null}
    </div>
  </div>
);
