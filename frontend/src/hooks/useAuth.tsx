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
import { isSessionExpiredError } from "@/utils/getApiErrorMessage";

interface AuthContextValue {
  user: UserProfile | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  isAuthLoading: boolean;
  login: (requestBody: LoginRequest) => Promise<void>;
  signup: (requestBody: SignupRequest) => Promise<void>;
  logout: () => Promise<void>;
  refreshCurrentUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [isAuthLoading, setIsAuthLoading] = useState(true);

  const logout = useCallback(async () => {
    try {
      // 서버가 HttpOnly 쿠키를 만료시킨다.
      await authService.logout();
    } catch {
      // 로그아웃은 멱등하다. 서버 호출이 실패(오프라인/네트워크/500)해도 삼킨다 —
      // 여기서 reject를 전파하면 호출부(AppLayout 리다이렉트, useApiErrorHandler)에서
      // 리다이렉트 누락·unhandled rejection이 발생한다. 클라이언트 상태만 확실히 비운다.
    } finally {
      setUser(null);
    }
  }, []);

  const refreshCurrentUser = useCallback(async () => {
    // 토큰은 HttpOnly 쿠키라 JS로 존재 여부를 알 수 없다 → 항상 /me를 호출해 확인한다.
    try {
      const currentUser = await authService.getCurrentUser();
      setUser(currentUser);
    } catch (error) {
      // 미인증(비로그인/만료)은 401 → 로드 시점에는 조용히 로그아웃 상태로 둔다.
      // "세션 만료" 안내는 사용 중 보호 API 호출이 401일 때 useApiErrorHandler가 담당한다.
      if (isSessionExpiredError(error)) {
        setUser(null);
        return;
      }

      throw error;
    } finally {
      setIsAuthLoading(false);
    }
  }, []);

  useEffect(() => {
    // 마운트 시 /me의 비-401 실패(500·네트워크)는 인증 여부를 단정할 수 없으므로
    // 게스트로 폴백한다(loading은 finally에서 종료됨). 유효 세션이 일시 장애로 게스트로
    // 보일 수 있으며, 복구는 다음 페이지 로드/새로고침에 refreshCurrentUser가 다시 돌아
    // /me가 성공할 때 일어난다(사용 중 보호 API 401은 로그아웃 안내만 하고 세션을 복원하진 않음).
    // login()은 refreshCurrentUser를 await하며 catch하지 않으므로 로그인 경로 에러는 화면에 노출된다.
    void refreshCurrentUser().catch(() => undefined);
  }, [refreshCurrentUser]);

  const login = useCallback(
    async (requestBody: LoginRequest) => {
      // 요청 헤더의 X-Guest-Token으로 서버가 게스트 카트를 회원 카트에 병합한다.
      // 토큰은 응답의 HttpOnly 쿠키로 심겼으므로 별도 저장이 필요 없다.
      await authService.login(requestBody);
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
