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
      // 요청 헤더의 X-Guest-Token으로 서버가 게스트 카트를 회원 카트에 병합한다.
      const tokenResponse = await authService.login(requestBody);
      localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, tokenResponse.accessToken);
      // 병합 완료 → 게스트 토큰 제거(이후 회원 카트 사용)
      localStorage.removeItem(STORAGE_KEYS.GUEST_TOKEN);
      await refreshCurrentUser();
    },
    [refreshCurrentUser],
  );

  const signup = useCallback(async (requestBody: SignupRequest) => {
    // 회원가입 시에도 X-Guest-Token으로 게스트 카트가 새 회원 카트에 병합된다.
    await authService.signup(requestBody);
    localStorage.removeItem(STORAGE_KEYS.GUEST_TOKEN);
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
