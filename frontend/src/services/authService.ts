import { API_ENDPOINTS } from "@/constants/api";
import type { LoginRequest, SignupRequest, UserProfile } from "@/types/auth";
import { apiClient } from "@/services/apiClient";

export const authService = {
  signup: (requestBody: SignupRequest): Promise<UserProfile> =>
    apiClient.post<UserProfile, SignupRequest>(API_ENDPOINTS.USERS.SIGNUP, requestBody),
  // 토큰은 HttpOnly 쿠키로만 내려오므로 응답 바디는 비어 있다.
  login: (requestBody: LoginRequest): Promise<void> =>
    apiClient.post<void, LoginRequest>(API_ENDPOINTS.USERS.LOGIN, requestBody),
  logout: (): Promise<void> =>
    apiClient.post<void, undefined>(API_ENDPOINTS.USERS.LOGOUT, undefined),
  getCurrentUser: (): Promise<UserProfile> =>
    apiClient.get<UserProfile>(API_ENDPOINTS.USERS.ME),
};
