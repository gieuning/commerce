import { useCallback } from "react";
import { STORAGE_KEYS } from "@/constants/storageKeys";
import { useAuth } from "@/hooks/useAuth";
import { getApiErrorMessage, isSessionExpiredError } from "@/utils/getApiErrorMessage";

export const useApiErrorHandler = () => {
  const { logout } = useAuth();

  return useCallback((error: unknown): string => {
    const errorMessage = getApiErrorMessage(error);

    if (isSessionExpiredError(error)) {
      sessionStorage.setItem(STORAGE_KEYS.AUTH_NOTICE, errorMessage);
      logout();
    }

    return errorMessage;
  }, [logout]);
};
