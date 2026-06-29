import { API_BASE_URL } from "@/constants/api";
import { STORAGE_KEYS } from "@/constants/storageKeys";
import type { ApiErrorResponse, ApiResponse } from "@/types/api";

type HttpMethod = "GET" | "POST" | "PUT" | "PATCH" | "DELETE";

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

  const parsedBody: unknown = JSON.parse(responseText);

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
