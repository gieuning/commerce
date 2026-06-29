import { useState } from "react";
import { MESSAGES } from "@/constants/messages";
import { ApiError } from "@/services/apiClient";

interface AsyncActionState {
  isLoading: boolean;
  errorMessage: string | null;
}

export const useAsyncAction = () => {
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
      const errorMessage =
        error instanceof ApiError ? error.message : MESSAGES.COMMON.UNKNOWN_ERROR;
      setActionState({ isLoading: false, errorMessage });
      return null;
    }
  };

  return {
    ...actionState,
    runAsyncAction,
    clearError: () => setActionState((currentState) => ({ ...currentState, errorMessage: null })),
  };
};
