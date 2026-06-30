import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { STORAGE_KEYS } from "@/constants/storageKeys";
import { USER_ROLES } from "@/constants/userRoles";
import { authService } from "@/services/authService";
import type { LoginRequest, SignupRequest, UserProfile } from "@/types/auth";
import { getApiErrorMessage, isSessionExpiredError } from "@/utils/getApiErrorMessage";

interface AuthContextValue {
  user: UserProfile | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  isAuthLoading: boolean;
  login: (requestBody: LoginRequest) => Promise<void>;
  signup: (requestBody: SignupRequest) => Promise<void>;
  logout: () => void;
  refreshCurrentUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [isAuthLoading, setIsAuthLoading] = useState(true);

  const logout = useCallback(() => {
    localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
    setUser(null);
  }, []);

  const refreshCurrentUser = useCallback(async () => {
    const accessToken = localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);

    if (!accessToken) {
      setUser(null);
      setIsAuthLoading(false);
      return;
    }

    try {
      const currentUser = await authService.getCurrentUser();
      setUser(currentUser);
    } catch (error) {
      if (isSessionExpiredError(error)) {
        sessionStorage.setItem(STORAGE_KEYS.AUTH_NOTICE, getApiErrorMessage(error));
        logout();
        return;
      }

      throw error;
    } finally {
      setIsAuthLoading(false);
    }
  }, [logout]);

  useEffect(() => {
    void refreshCurrentUser().catch(() => undefined);
  }, [refreshCurrentUser]);

  const login = useCallback(
    async (requestBody: LoginRequest) => {
      const tokenResponse = await authService.login(requestBody);
      localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, tokenResponse.accessToken);
      await refreshCurrentUser();
    },
    [refreshCurrentUser],
  );

  const signup = useCallback(async (requestBody: SignupRequest) => {
    await authService.signup(requestBody);
  }, []);

  const contextValue = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: user !== null,
      isAdmin: user?.role === USER_ROLES.ADMIN,
      isAuthLoading,
      login,
      signup,
      logout,
      refreshCurrentUser,
    }),
    [isAuthLoading, login, logout, refreshCurrentUser, signup, user],
  );

  return <AuthContext.Provider value={contextValue}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextValue => {
  const authContext = useContext(AuthContext);

  if (!authContext) {
    throw new Error("useAuth는 AuthProvider 내부에서만 사용할 수 있습니다.");
  }

  return authContext;
};
