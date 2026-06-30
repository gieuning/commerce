import { useState } from "react";
import { useApiErrorHandler } from "@/hooks/useApiErrorHandler";

interface AsyncActionState {
  isLoading: boolean;
  errorMessage: string | null;
}

export const useAsyncAction = () => {
  const handleApiError = useApiErrorHandler();
  const [actionState, setActionState] = useState<AsyncActionState>({
    isLoading: false,
    errorMessage: null,
  });

  const runAsyncAction = async <TResult,>(action: () => Promise<TResult>): Promise<TResult | null> => {
    setActionState({ isLoading: true, errorMessage: null });

    try {
      const actionResult = await action();
      setActionState({ isLoading: false, errorMessage: null });
      return actionResult;
    } catch (error: unknown) {
      setActionState({ isLoading: false, errorMessage: handleApiError(error) });
      return null;
    }
  };

  return {
    ...actionState,
    runAsyncAction,
    clearError: () => setActionState((currentState) => ({ ...currentState, errorMessage: null })),
  };
};
