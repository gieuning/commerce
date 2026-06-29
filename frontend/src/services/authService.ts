import { API_ENDPOINTS } from "@/constants/api";
import type { LoginRequest, SignupRequest, TokenResponse, UserProfile } from "@/types/auth";
import { apiClient } from "@/services/apiClient";

export const authService = {
  signup: (requestBody: SignupRequest): Promise<UserProfile> =>
    apiClient.post<UserProfile, SignupRequest>(API_ENDPOINTS.USERS.SIGNUP, requestBody),
  login: (requestBody: LoginRequest): Promise<TokenResponse> =>
    apiClient.post<TokenResponse, LoginRequest>(API_ENDPOINTS.USERS.LOGIN, requestBody),
  getCurrentUser: (): Promise<UserProfile> =>
    apiClient.get<UserProfile>(API_ENDPOINTS.USERS.ME),
};
