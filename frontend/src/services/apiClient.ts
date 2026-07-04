import { API_BASE_URL } from "@/constants/api";
import { STORAGE_KEYS } from "@/constants/storageKeys";
import type { ApiErrorResponse, ApiResponse } from "@/types/api";

type HttpMethod = "GET" | "POST" | "PUT" | "PATCH" | "DELETE";

const GUEST_TOKEN_HEADER = "X-Guest-Token";

interface RequestOptions<TBody> {
  method: HttpMethod;
  body?: TBody;
}

export class ApiError extends Error {
  readonly errorCode: string;
  readonly status: number;

  constructor(errorResponse: ApiErrorResponse, status: number) {
    super(errorResponse.errorMessage);
    this.name = "ApiError";
    this.errorCode = errorResponse.errorCode;
    this.status = status;
  }
}

const isApiResponse = <TResponse>(value: unknown): value is ApiResponse<TResponse> => {
  if (typeof value !== "object" || value === null) {
    return false;
  }

  return "data" in value || "error" in value;
};

const createHeaders = (): Headers => {
  const requestHeaders = new Headers({
    "Content-Type": "application/json",
  });
  const accessToken = localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);

  if (accessToken) {
    requestHeaders.set("Authorization", `Bearer ${accessToken}`);
  }

  // 게스트 장바구니 식별용. 회원은 서버가 JWT를 우선하므로 함께 보내도 무해하다.
  const guestToken = localStorage.getItem(STORAGE_KEYS.GUEST_TOKEN);

  if (guestToken) {
    requestHeaders.set(GUEST_TOKEN_HEADER, guestToken);
  }

  return requestHeaders;
};

const parseResponseBody = async <TResponse>(
  response: Response,
): Promise<ApiResponse<TResponse>> => {
  if (response.status === 204) {
    return {};
  }

  const responseText = await response.text();

  if (!responseText) {
    return {};
  }

  let parsedBody: unknown;

  try {
    parsedBody = JSON.parse(responseText);
  } catch {
    return { data: responseText as TResponse };
  }

  if (!isApiResponse<TResponse>(parsedBody)) {
    return { data: parsedBody as TResponse };
  }

  return parsedBody;
};

const request = async <TResponse, TBody = unknown>(
  endpoint: string,
  options: RequestOptions<TBody>,
): Promise<TResponse> => {
  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    method: options.method,
    headers: createHeaders(),
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  });

  // 서버가 게스트 첫 담기 시 발급한 토큰을 저장해 이후 요청에 재사용한다.
  const issuedGuestToken = response.headers.get(GUEST_TOKEN_HEADER);

  if (issuedGuestToken) {
    localStorage.setItem(STORAGE_KEYS.GUEST_TOKEN, issuedGuestToken);
  }

  const responseBody = await parseResponseBody<TResponse>(response);

  if (!response.ok || responseBody.error) {
    const errorResponse = responseBody.error ?? {
      errorCode: "HTTP_ERROR",
      errorMessage: response.statusText,
    };
    throw new ApiError(errorResponse, response.status);
  }

  return responseBody.data as TResponse;
};

export const apiClient = {
  get: <TResponse>(endpoint: string): Promise<TResponse> =>
    request<TResponse>(endpoint, { method: "GET" }),
  post: <TResponse, TBody>(endpoint: string, body: TBody): Promise<TResponse> =>
    request<TResponse, TBody>(endpoint, { method: "POST", body }),
  put: <TResponse, TBody>(endpoint: string, body: TBody): Promise<TResponse> =>
    request<TResponse, TBody>(endpoint, { method: "PUT", body }),
  patch: <TResponse, TBody>(endpoint: string, body: TBody): Promise<TResponse> =>
    request<TResponse, TBody>(endpoint, { method: "PATCH", body }),
  delete: <TResponse>(endpoint: string): Promise<TResponse> =>
    request<TResponse>(endpoint, { method: "DELETE" }),
};
