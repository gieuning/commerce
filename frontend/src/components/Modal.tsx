import { useEffect } from "react";
import { Button } from "@/components/Button";

interface ModalProps {
  isOpen: boolean;
  title: string;
  description?: string;
  confirmLabel?: string;
  onConfirm: () => void;
}

export const Modal = ({
  isOpen,
  title,
  description,
  confirmLabel = "확인",
  onConfirm,
}: ModalProps) => {
  useEffect(() => {
    if (!isOpen) {
      return;
    }

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        onConfirm();
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [isOpen, onConfirm]);

  if (!isOpen) {
    return null;
  }

  return (
    <div
      aria-labelledby="modal-title"
      aria-modal="true"
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      onClick={onConfirm}
      role="dialog"
    >
      <div
        className="w-full max-w-sm rounded-card bg-surface p-6 text-center shadow-xl"
        onClick={(event) => event.stopPropagation()}
      >
        <h2 className="text-lg font-bold text-ink" id="modal-title">
          {title}
        </h2>
        {description ? (
          <p className="mt-2 text-sm leading-6 text-ink-soft">{description}</p>
        ) : null}
        <Button autoFocus className="mt-6 w-full" onClick={onConfirm}>
          {confirmLabel}
        </Button>
      </div>
    </div>
  );
};
