import type { ReactNode } from "react";
import { act, renderHook, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { USER_ROLES } from "@/constants/userRoles";
import { AuthProvider, useAuth } from "@/hooks/useAuth";
import { ApiError } from "@/services/apiClient";
import { authService } from "@/services/authService";
import type { UserProfile } from "@/types/auth";

vi.mock("@/services/authService", () => ({
  authService: {
    signup: vi.fn(),
    login: vi.fn(),
    logout: vi.fn(),
    getCurrentUser: vi.fn(),
  },
}));

const authServiceMock = vi.mocked(authService);

const AUTHENTICATED_USER: UserProfile = {
  id: 1,
  email: "user@example.com",
  name: "테스터",
  phoneNumber: null,
  role: USER_ROLES.USER,
};

const wrapper = ({ children }: { children: ReactNode }) => <AuthProvider>{children}</AuthProvider>;

const renderAuthenticated = async () => {
  authServiceMock.getCurrentUser.mockResolvedValue(AUTHENTICATED_USER);
  const rendered = renderHook(() => useAuth(), { wrapper });
  await waitFor(() => expect(rendered.result.current.isAuthenticated).toBe(true));
  return rendered;
};

describe("useAuth logout", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    sessionStorage.clear();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it("서버 로그아웃이 실패해도 클라이언트 상태를 반드시 비운다", async () => {
    const { result } = await renderAuthenticated();
    authServiceMock.logout.mockRejectedValue(new Error("network down"));

    await act(async () => {
      // reject를 전파하지 않아야 한다 — 전파 시 호출부 리다이렉트 누락/unhandled rejection 발생.
      await expect(result.current.logout()).resolves.toBeUndefined();
    });

    expect(result.current.user).toBeNull();
    expect(result.current.isAuthenticated).toBe(false);
  });

  it("서버 로그아웃 성공 시에도 상태를 비운다", async () => {
    const { result } = await renderAuthenticated();
    authServiceMock.logout.mockResolvedValue(undefined);

    await act(async () => {
      await result.current.logout();
    });

    expect(authServiceMock.logout).toHaveBeenCalledOnce();
    expect(result.current.user).toBeNull();
  });
});

describe("useAuth 초기 인증 확인", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    sessionStorage.clear();
  });

  it("마운트 시 401이면 게스트 상태로 두고 로딩을 종료한다", async () => {
    authServiceMock.getCurrentUser.mockRejectedValue(
      new ApiError({ errorCode: "MISSING_TOKEN", errorMessage: "인증이 필요합니다." }, 401),
    );

    const { result } = renderHook(() => useAuth(), { wrapper });

    await waitFor(() => expect(result.current.isAuthLoading).toBe(false));
    expect(result.current.user).toBeNull();
    expect(result.current.isAuthenticated).toBe(false);
  });
});
