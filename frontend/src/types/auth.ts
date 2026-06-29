import type { UserRole } from "@/constants/userRoles";

export interface SignupRequest {
  email: string;
  password: string;
  name: string;
  phoneNumber?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface TokenResponse {
  accessToken: string;
}

export interface UserProfile {
  id: number;
  email: string;
  name: string;
  phoneNumber: string | null;
  role: UserRole;
}
